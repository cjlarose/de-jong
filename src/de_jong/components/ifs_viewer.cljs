(ns de-jong.components.ifs-viewer
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om]
            [om.dom :as dom]
            [cljs.core.async :refer [<! chan put!]]
            [cljsjs.three]
            [de-jong.constants :refer [points-to-draw]]
            [de-jong.util :refer [write-values!]]))

(defn handle-resize [owner _]
  (let [{:keys [renderer camera]} (om/get-state owner)
        w (.-innerWidth js/window)
        h (.-innerHeight js/window)]
    (set! (.-aspect camera) (/ w h))
    (.updateProjectionMatrix camera)
    (.setSize renderer w h)))

(def color-arr
  (let [arr      (js/Float32Array. (* 3 points-to-draw))
        size     (.sqrt js/Math points-to-draw)
        size-inv (/ 1 size)
        colors   (for [x (range size) y (range size)]
                   [(* x size-inv) 1 (* y size-inv)])
        rgbs     (->> colors
                   (map (fn [[h s l]] (.setHSL (js/THREE.Color.) h s l)))
                   (map (fn [c] [(.-r c) (.-g c) (.-b c)])))]
    ; (println rgbs)
    ; (println (count rgbs))
    (write-values! arr rgbs)
    arr))

; (.log js/console color-arr)
; (println color-arr)

(defn draw! [owner draw-chan]
  (go (while true
    (let [points (<! draw-chan)]
      (if-not (nil? points)
        (let [vertex-attr (js/THREE.BufferAttribute. points 3)
              {:keys [geometry renderer scene camera cloud]} (om/get-state owner)]
          (.addAttribute geometry "position" vertex-attr)
          (.render renderer scene camera)))))))

(defn ifs-viewer [draw-chan owner]
  (reify
    om/IInitState
    (init-state [_]
      (let [width    (.-innerWidth js/window)
            height   (.-innerHeight js/window)
            geometry (js/THREE.BufferGeometry.)
            scene    (js/THREE.Scene.)
            camera   (js/THREE.PerspectiveCamera. 45 (/ width height) 0.1 1000)
            material (js/THREE.PointCloudMaterial. #js { :size 0.02 :vertexColors js/THREE.VertexColors})
            cloud    (js/THREE.PointCloud. geometry material)]
        (.add scene cloud)
        (.addAttribute geometry "color" (js/THREE.BufferAttribute. color-arr 3))
        (set! (.-z (.-position camera)) 8)
        { :geometry geometry
          :scene scene
          :camera camera
          :cloud cloud }))
    om/IDidMount
    (did-mount [_]
      (let [renderer (js/THREE.WebGLRenderer. #js { :canvas (om/get-node owner "canvas")
                                                    :alpha true })]
        (.setSize renderer (.-innerWidth js/window) (.-innerHeight js/window))
        (.addEventListener js/window "resize" (partial handle-resize owner))
        (draw! owner draw-chan)
        (om/update-state! owner (fn [prev] (merge prev { :renderer renderer })))))
    om/IRender
    (render [_]
      (dom/div #js {:id "ifs-viewer"}
        (dom/canvas #js { :ref "canvas" })))))
