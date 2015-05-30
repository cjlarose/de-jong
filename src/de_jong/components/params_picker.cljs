(ns de-jong.components.params-picker
  (:require [om.core :as om]
            [om.dom :as dom]))

(defn slider [{ :keys [value on-change] } owner]
  (reify
    om/IRender
    (render [this]
      (dom/input #js { :type "range"
                       :min (- js/Math.PI)
                       :max js/Math.PI
                       :step 0.001
                       :onChange (fn [e] (on-change (js/parseFloat (.-value (.-target e)))))
                       :value value }))))

(defn param-picker [{:keys [label value on-change]} owner]
  (reify
    om/IRender
    (render [this]
      (dom/li #js { :className "param-picker" }
        (dom/span #js { :className "param-label" } label)
        (om/build slider { :value value :on-change on-change })
        (dom/span #js { :className "param-value" } (.toFixed value 3))))))

(defn params-picker [params owner]
  (reify
    om/IRender
    (render [this]
      (let [param-labels ["α" "β" "γ" "δ"]
            handle-change (fn [i v]
                            (om/transact! params (fn [prev] (assoc prev i v))))
            picker-props (fn [i label v]
                           { :label label
                             :value v
                             :on-change (partial handle-change i) })]
        (dom/section #js { :className "params-picker" }
          (apply dom/ul nil
            (om/build-all param-picker (map picker-props (range 4) param-labels params))))))))

