(ns de-jong.ifs-viewer
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om]
            [om.dom :as dom]
            [cljs.core.async :refer [<! chan put!]]
            [cljsjs.three]))

(defn handle-resize [owner e]
  (om/update-state!
    owner
    (fn [{:keys [renderer camera] :as prev}]
      (let [w (.-innerWidth js/window)
            h (.-innerHeight js/window)]
        (set! (.-aspect camera) (/ w h))
        (.updateProjectionMatrix camera)
        (.setSize renderer w h)
        (merge prev { :width w :height h })))))

(defn animation-frame
  ([]
    (animation-frame (chan)))
  ([comm]
    (put! comm (.requestAnimationFrame js/window (fn [_] (animation-frame comm))))
    comm))

(defn draw! [owner draw-chan]
  (let [throttler (animation-frame)]
    (go (while true
      (<! throttler)
      (let [points (<! draw-chan)
            vertex-attr (js/THREE.BufferAttribute. points 3)
            {:keys [geometry renderer scene camera cloud]} (om/get-state owner)]
        (.addAttribute geometry "position" vertex-attr)
        (set! (.-y (.-rotation cloud)) (+ 0.01 (.-y (.-rotation cloud))))
        (set! (.-z (.-rotation cloud)) (+ 0.01 (.-z (.-rotation cloud))))
        (.render renderer scene camera))))))

(defn ifs-viewer [draw-chan owner]
  (reify
    om/IInitState
    (init-state [_]
      (let [width    (.-innerWidth js/window)
            height   (.-innerHeight js/window)
            geometry (js/THREE.BufferGeometry.)
            scene    (js/THREE.Scene.)
            camera   (js/THREE.PerspectiveCamera. 45 (/ width height) 0.1 1000)
            material (js/THREE.PointCloudMaterial. #js { :size 0.02 :color 0x00cc00 })
            cloud    (js/THREE.PointCloud. geometry material)]
        (.add scene cloud)
        (set! (.-z (.-position camera)) 8)
        { :geometry geometry
          :scene scene
          :camera camera
          :cloud cloud
          :width width
          :height height }))
    om/IDidMount
    (did-mount [_]
      (let [renderer (js/THREE.WebGLRenderer. #js { :canvas (om/get-node owner "canvas")
                                                    :alpha true })]
        (.setSize renderer (om/get-state owner :width) (om/get-state owner :height))
        (.addEventListener js/window "resize" (partial handle-resize owner))
        (draw! owner draw-chan)
        (om/update-state! owner (fn [prev] (merge prev { :renderer renderer })))))
    om/IRenderState
    (render-state [_ state]
      (dom/div #js {:id "ifs-viewer"}
        (dom/canvas #js {:ref "canvas" :width (:width state) :height (:height state)})))))
