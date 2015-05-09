(ns de-jong.ifs-viewer
  (:require [om.core :as om]
            [om.dom :as dom]
            [cljsjs.three]))

(defn points-to-vertices [points]
  (apply array (map (fn [[x y z]] (js/THREE.Vector3. x y z)) points)))

(defn ifs-viewer [points owner]
  (let [w 800 h 800]
    (reify
      om/IInitState
      (init-state [_]
        (let [geometry (js/THREE.Geometry.)
              scene    (js/THREE.Scene.)
              camera   (js/THREE.PerspectiveCamera. 45 (/ w h) 0.1 1000)
              material (js/THREE.PointCloudMaterial. #js { :size 0.02 :color 0x00cc00 })
              cloud    (js/THREE.PointCloud. geometry material)]
          (.add scene cloud)
          (set! (.-z (.-position camera)) 8)
          { :geometry geometry
            :scene scene
            :camera camera
            :cloud cloud }))
      om/IDidMount
      (did-mount [_]
        (let [renderer (js/THREE.WebGLRenderer. #js { :canvas (om/get-node owner "canvas")
                                                      :alpha true })]
          (om/update-state! owner (fn [prev] (merge prev { :renderer renderer })))))
      om/IDidUpdate
      (did-update [_ _ _]
        (let [{:keys [geometry renderer scene camera cloud]} (om/get-state owner)]
          (set! (.-vertices geometry) (points-to-vertices points))
          (set! (.-verticesNeedUpdate geometry) true)
          (set! (.-y (.-rotation cloud)) (+ 0.01 (.-y (.-rotation cloud))))
          (set! (.-z (.-rotation cloud)) (+ 0.01 (.-z (.-rotation cloud))))
          (.render renderer scene camera)))
      om/IRender
      (render [_]
        (dom/div #js {:id "ifs-viewer"}
          (dom/canvas #js {:ref "canvas" :width w :height h}))))))
