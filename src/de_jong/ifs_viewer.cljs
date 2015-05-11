(ns de-jong.ifs-viewer
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om]
            [om.dom :as dom]
            [cljs.core.async :refer [<! chan put!]]
            [cljsjs.three]))

(defn points-to-vertices [points]
  (apply array (map (fn [[x y z]] (js/THREE.Vector3. x y z)) points)))

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

(defn animation-frame [timeout-chan]
  (let [ts (.requestAnimationFrame js/window #(animation-frame timeout-chan))]
    (put! timeout-chan ts)))

(defn draw! [owner draw-chan]
  (let [throttler (chan)]
    (animation-frame throttler)
    (go (while true
      (<! throttler)
      (let [points (<! draw-chan)
            {:keys [geometry renderer scene camera cloud]} (om/get-state owner)]
        (set! (.-vertices geometry) (points-to-vertices points))
        (set! (.-verticesNeedUpdate geometry) true)
        (set! (.-y (.-rotation cloud)) (+ 0.01 (.-y (.-rotation cloud))))
        (set! (.-z (.-rotation cloud)) (+ 0.01 (.-z (.-rotation cloud))))
        (.render renderer scene camera))))))

(defn ifs-viewer [draw-chan owner]
  (reify
    om/IInitState
    (init-state [_]
      (let [width    (.-innerWidth js/window)
            height   (.-innerHeight js/window)
            geometry (js/THREE.Geometry.)
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
