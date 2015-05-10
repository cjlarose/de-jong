(ns de-jong.params-picker
  (:require [om.core :as om]
            [om.dom :as dom]))

(defn handle-click [owner on-change e]
  (let [rect  (.getBoundingClientRect (.-target e))
        cx    (/ (+ (.-left rect) (.-right rect)) 2)
        cy    (/ (+ (.-top rect) (.-bottom rect)) 2)
        x     (.-clientX e)
        y     (.-clientY e)
        angle (js/Math.atan2 (- y cy) (- x cx))]
    (on-change angle)))

(defn circular-slider [{:keys [value diameter on-change] :or {diameter 100}} owner]
  (reify
    om/IRender
    (render [this]
      (dom/div
        #js { :style #js { :width diameter
                           :height diameter
                           :transform (str "rotate(" value "rad)") }
              :onClick (partial handle-click owner on-change)
              :className "circular-slider" } ))))

(defn slider [label value on-change]
  (dom/li #js { :className "param-picker" }
    (dom/div #js { :className "param-display" }
      (str label ": " (.toFixed value 3)))
    (om/build circular-slider {:value value :on-change on-change})))

(defn param-picker [{:keys [label value on-change]} owner]
  (reify
    om/IRender
    (render [this]
      (slider label value on-change))))

(defn handle-change [params i v]
  (om/transact! params (fn [prev] (assoc prev i v))))

(defn params-picker [params owner]
  (reify
    om/IRender
    (render [this]
      (let [param-labels ["α" "β" "γ" "δ"]
            picker-props (fn [i label v] { :label label
                                           :value v
                                           :on-change (partial handle-change params i) })]
        (dom/section #js {:id "params-picker"}
          (apply dom/ul nil
            (om/build-all param-picker (map picker-props (range 4) param-labels params))))))))

