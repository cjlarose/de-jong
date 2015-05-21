(ns de-jong.components.point-cloud
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om]
            [om.dom :as dom]
            [cljs.core.async :refer [<! chan put!]]))

(defn handle-resize [owner _]
  (let [{:keys [renderer camera]} (om/get-state owner)
        w (.-innerWidth js/window)
        h (.-innerHeight js/window)]
    (set! (.-aspect camera) (/ w h))
    (.updateProjectionMatrix camera)
    (.setSize renderer w h)))

(defn setup-canvas! [canvas]
  (let [context (.getContext canvas "2d")]
    (set! (.-fillStyle context) "rgba(0, 192, 0, 0.5)")
    (.scale context 200 200)
    (.translate context 2 2)))

(defn draw! [owner draw-chan]
  (go (while true
    (let [points (<! draw-chan)]
      (if-not (nil? points)
        (let [canvas (om/get-node owner "canvas")
              context (.getContext canvas "2d")
              { :keys [width height] } (om/get-state owner)
              image-data (.createImageData context width height)]
          (.clearRect context -2 -2 4 4)
          (doseq [[x y _] (partition 3 (array-seq points))]
            (let [fx (js/Math.floor (* 200 (+ x 2)))
                  fy (js/Math.floor (* 200 (+ y 2)))]
              (aset (.-data image-data) (* (+ (* fy width) fx) 4) 255)
              (aset (.-data image-data) (+ (* (+ (* fy width) fx) 4) 3) 255)))
          (.putImageData context image-data 0 0)))))))

(defn point-cloud [draw-chan owner]
  (reify
    om/IInitState
    (init-state [_]
      (let [width    (.-innerWidth js/window)
            height   (.-innerHeight js/window)]
        { :width width
          :height height }))
    om/IDidMount
    (did-mount [_]
        (setup-canvas! (om/get-node owner "canvas"))
        (draw! owner draw-chan))
    om/IRenderState
    (render-state [_ { :keys [width height] }]
      (dom/div #js {:id "point-cloud"}
        (dom/canvas #js { :ref "canvas" :width width :height height})))))
