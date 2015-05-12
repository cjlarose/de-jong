(ns de-jong.circular-slider
  (:require [om.core :as om]
            [om.dom :as dom]))

(defn get-angle [e]
  (let [rect  (.getBoundingClientRect (.-target e))
        cx    (/ (+ (.-left rect) (.-right rect)) 2)
        cy    (/ (+ (.-top rect) (.-bottom rect)) 2)
        x     (.-clientX e)
        y     (.-clientY e)
        angle (js/Math.atan2 (- y cy) (- x cx))]
    angle))

(defn circular-slider [{:keys [value diameter on-change] :or {diameter 100}} owner]
  (reify
    om/IInitState
    (init-state [_]
      { :listening false })
    om/IRenderState
    (render-state [this {:keys [listening]}]
      (let [stop-listening! #(om/set-state! owner :listening false)]
        (dom/div
          #js { :style #js { :width diameter
                             :height diameter
                             :transform (str "rotate(" value "rad)") }
                :onMouseDown (fn [e]
                               (om/set-state! owner :listening true)
                               (on-change (get-angle e)))
                :onMouseMove (fn [e]
                               (if listening
                                 (on-change (get-angle e))))
                :onMouseUp    (stop-listening!)
                :onMouseLeave (stop-listening!)
                :className (str "circular-slider" (if listening " circular-slider-grabbing")) } )))))
