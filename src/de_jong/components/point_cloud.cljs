(ns de-jong.components.point-cloud
  (:require [om.core :as om]
            [om.dom :as dom]
            [cljsjs.three]
            [de-jong.points-calculator :refer [random-vertex-array]]))

(def vertex-shader
  "uniform float pointSize;
   uniform float deJongParams[4];
   void main() {
     gl_PointSize = pointSize;

     float x = position.x, y = position.y, tmpX, tmpY;

     for (int i = 0; i < 5; i++) {
       tmpX = x, tmpY = y;
       x = sin(deJongParams[0] * tmpY) - cos(deJongParams[1] * tmpX);
       y = sin(deJongParams[2] * tmpX) - cos(deJongParams[3] * tmpY);
     }

     vec4 newPos = vec4(x, y, 0, 1.0);
     gl_Position = projectionMatrix * modelViewMatrix * newPos;
   }")

(def fragment-shader
  "void main() {
     gl_FragColor = vec4(0.0, 1.0, 0.0, 0.25);
   }")

(defn point-cloud [{ :keys [de-jong-params width height point-size num-points]
                     :or { point-size 1.0 } } owner]
  (reify
    om/IInitState
    (init-state [_]
      (let [geometry (js/THREE.BufferGeometry.)
            scene    (js/THREE.Scene.)
            camera   (js/THREE.PerspectiveCamera. 45 (/ width height) 0.1 1000)
            uniforms #js { :pointSize #js { :type "f" :value point-size }
                           :deJongParams #js { :type "fv1" :value (clj->js de-jong-params) } }
            material (js/THREE.ShaderMaterial. #js { :transparent true
                                                     :uniforms uniforms
                                                     :vertexShader vertex-shader
                                                     :fragmentShader fragment-shader })
            cloud    (js/THREE.PointCloud. geometry material)
            points   (random-vertex-array num-points -2.0 2.0)
            vertex-attr (js/THREE.BufferAttribute. points 3)]
        (.add scene cloud)
        (set! (.-z (.-position camera)) 8)
        (.addAttribute geometry "position" vertex-attr)
        { :scene scene
          :camera camera
          :uniforms uniforms }))
    om/IWillReceiveProps
    (will-receive-props [_ { :keys [de-jong-params height width] }]
      (let [{ old-height :height old-width :width } (om/get-props owner)
            { :keys [uniforms camera renderer] }    (om/get-state owner)]
        (set! (.-value (.-deJongParams uniforms)) (clj->js de-jong-params))
        (if-not (and (= old-height height) (= old-width width))
          (do (set! (.-aspect camera) (/ width height))
              (.updateProjectionMatrix camera)
              (.setSize renderer width height)))))
    om/IDidMount
    (did-mount [_]
      (let [renderer (js/THREE.WebGLRenderer. #js { :canvas (om/get-node owner "canvas")
                                                    :alpha true })]
        (.setSize renderer width height)
        (om/update-state! owner (fn [prev] (merge prev { :renderer renderer })))))
    om/IDidUpdate
    (did-update [_ _ _]
      (let [{ :keys [renderer scene camera] } (om/get-state owner)]
        (.render renderer scene camera)))
    om/IRender
    (render [_]
      (dom/div #js {:className "point-cloud"}
        (dom/canvas #js { :ref "canvas" })))))

(defn screen-dimensions []
  { :width  (.-innerWidth js/window)
    :height (.-innerHeight js/window) })

(defn handle-window-resize [owner _]
  (om/update-state! owner #(screen-dimensions)))

(defn full-screen-point-cloud [[de-jong-params num-points] owner]
  (reify
    om/IInitState
    (init-state [_]
      (screen-dimensions))
    om/IDidMount
    (did-mount [_]
      (.addEventListener js/window "resize" (partial handle-window-resize owner)))
    om/IRenderState
    (render-state [this { :keys [width height] } ]
      (om/build point-cloud { :num-points num-points
                              :de-jong-params de-jong-params
                              :width width
                              :height height }))))
