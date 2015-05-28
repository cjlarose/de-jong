(ns de-jong.components.point-cloud
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om]
            [om.dom :as dom]
            [cljs.core.async :refer [<! chan put!]]
            [cljsjs.three]
            [de-jong.points-calculator :refer [points-to-draw
                                               de-jong-ifs
                                               random-vertex-array
                                               vertices-apply]]
            ))

; (defn draw! [owner draw-chan]
;   (go (while true
;     (let [points (<! draw-chan)]
;       (if-not (nil? points)
;         (let [vertex-attr (js/THREE.BufferAttribute. points 3)
;               {:keys [geometry renderer scene camera cloud]} (om/get-state owner)]
;           (.addAttribute geometry "position" vertex-attr)
;           (.render renderer scene camera)))))))

(def points (random-vertex-array points-to-draw -2.0 2.0))

(defn draw! [owner draw-chan]
  (go (while true
    (let [params (<! draw-chan)]
      (if-not (nil? params)
        (let [
              vertex-attr (js/THREE.BufferAttribute. points 3)
              {:keys [geometry renderer scene camera cloud uniforms]} (om/get-state owner)]
          ; (println uniforms)
          ;(println params)
          (set! (.-value (.-deJongParams uniforms)) (clj->js params))
          (.addAttribute geometry "position" vertex-attr)
          (.render renderer scene camera)))))))

(def vertex-shader
  "uniform float deJongParams[4];
   void main() {
     gl_PointSize = deJongParams[0];
     gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
   }")

(def fragment-shader
  "void main() {
     gl_FragColor = vec4(0.0, 1.0, 0.0, 1.0);
   }")

(defn point-cloud [{ :keys [draw-chan width height point-size]
                     :or { point-size 0.02 } } owner]
  (reify
    om/IInitState
    (init-state [_]
      (let [geometry (js/THREE.BufferGeometry.)
            scene    (js/THREE.Scene.)
            camera   (js/THREE.PerspectiveCamera. 45 (/ width height) 0.1 1000)
            uniforms #js { :deJongParams #js { :type "fv1" :value #js [0 0 0 0] } }
            material (js/THREE.ShaderMaterial. #js { :uniforms uniforms
                                                     :vertexShader vertex-shader
                                                     :fragmentShader fragment-shader })
            cloud    (js/THREE.PointCloud. geometry material)]
        (.add scene cloud)
        (set! (.-z (.-position camera)) 8)
        { :geometry geometry
          :scene scene
          :camera camera
          :cloud cloud
          :uniforms uniforms }))
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
