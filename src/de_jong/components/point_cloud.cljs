(ns de-jong.components.point-cloud
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om]
            [om.dom :as dom]
            [cljs.core.async :refer [<! chan put!]]
            [cljsjs.three]
            [de-jong.points-calculator :refer [points-to-draw
                                               random-vertex-array]]))

(def points (random-vertex-array points-to-draw -2.0 2.0))

(defn draw! [owner draw-chan]
  (go (while true
    (let [params (<! draw-chan)]
      (if-not (nil? params)
        (let [{:keys [renderer scene camera uniforms]} (om/get-state owner)]
          (set! (.-value (.-deJongParams uniforms)) (clj->js params))
          (.render renderer scene camera)))))))

(def vertex-shader
  "uniform float deJongParams[4];
   void main() {
     gl_PointSize = 1.0;

     float x = position.x;
     float y = position.y;
     for (int i = 0; i < 5; i++) {
       x = sin(deJongParams[0] * y) - cos(deJongParams[1] * x);
       y = sin(deJongParams[2] * x) - cos(deJongParams[3] * y);
     }

     vec4 newPos = vec4(x, y, 0, 1.0);
     gl_Position = projectionMatrix * modelViewMatrix * newPos;
   }")

(def fragment-shader
  "void main() {
     gl_FragColor = vec4(0.0, 1.0, 0.0, 0.5);
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
            material (js/THREE.ShaderMaterial. #js { :transparent true
                                                     ; :opacity 0.75
                                                     ; :blending js/THREE.AdditiveBlending
                                                     ; :blending js/THREE.AdditiveBlending
                                                     :uniforms uniforms
                                                     :vertexShader vertex-shader
                                                     :fragmentShader fragment-shader })
            cloud    (js/THREE.PointCloud. geometry material)
            vertex-attr (js/THREE.BufferAttribute. points 3)]
        (.add scene cloud)
        (set! (.-z (.-position camera)) 8)
        (.addAttribute geometry "position" vertex-attr)
        { :scene scene
          :camera camera
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
