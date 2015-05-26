(ns de-jong.components.point-cloud
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om]
            [om.dom :as dom]
            [cljs.core.async :refer [<! chan put!]]
            [cljsjs.three]))

(defn draw! [owner draw-chan]
  (go (while true
    (let [points (<! draw-chan)]
      (if-not (nil? points)
        (let [vertex-attr (js/THREE.BufferAttribute. points 3)
              {:keys [geometry renderer scene camera cloud]} (om/get-state owner)]
          (.addAttribute geometry "position" vertex-attr)
          (.render renderer scene camera)))))))

(defn point-cloud [{ :keys [draw-chan width height] } owner]
  (reify
    om/IInitState
    (init-state [_]
      (let [geometry (js/THREE.BufferGeometry.)
            scene    (js/THREE.Scene.)
            camera   (js/THREE.PerspectiveCamera. 45 (/ width height) 0.1 1000)
            material (js/THREE.PointCloudMaterial. #js { :size 0.02 :color 0x00cc00 })
            cloud    (js/THREE.PointCloud. geometry material)]
        (.add scene cloud)
        (set! (.-z (.-position camera)) 8)
        { :geometry geometry
          :scene scene
          :camera camera
          :cloud cloud }))
    om/IWillReceiveProps
    (will-receive-props [this { :keys [width height] }]
      (let [{ :keys [renderer camera] } (om/get-state owner)]
        (set! (.-aspect camera) (/ width height))
        (.updateProjectionMatrix camera)
        (.setSize renderer width height)))
    om/IDidMount
    (did-mount [_]
      (let [renderer (js/THREE.WebGLRenderer. #js { :canvas (om/get-node owner "canvas")
                                                    :alpha true })]
        (.setSize renderer width height)
        (draw! owner draw-chan)
        (om/update-state! owner (fn [prev] (merge prev { :renderer renderer })))))
    om/IRender
    (render [_]
      (dom/div #js {:className "point-cloud"}
        (dom/canvas #js { :ref "canvas" })))))

(defn screen-dimensions []
  { :width  (.-innerWidth js/window)
    :height (.-innerHeight js/window) })

(defn handle-window-resize [owner _]
  (om/update-state! owner #(screen-dimensions)))

(defn full-screen-point-cloud [draw-chan owner]
  (reify
    om/IInitState
    (init-state [_]
      (screen-dimensions))
    om/IDidMount
    (did-mount [_]
      (.addEventListener js/window "resize" (partial handle-window-resize owner)))
    om/IRenderState
    (render-state [this { :keys [width height] } ]
      (om/build point-cloud { :draw-chan draw-chan
                              :width width
                              :height height }))))
