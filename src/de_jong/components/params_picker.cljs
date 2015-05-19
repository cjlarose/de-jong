(ns de-jong.components.params-picker
  (:require [om.core :as om]
            [om.dom :as dom]
            [de-jong.components.circular-slider :refer [circular-slider]]))

(defn param-picker [{:keys [label value on-change]} owner]
  (reify
    om/IRender
    (render [this]
      (dom/li #js { :className "param-picker" }
        (dom/div #js { :className "param-display" }
          (dom/span #js { :className "param-label" } label)
          (dom/span #js { :className "param-value" } (.toFixed value 3)))
        (om/build circular-slider {:value value :on-change on-change})))))

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
        (dom/section #js { :className "params-picker noselect" }
          (apply dom/ul nil
            (om/build-all param-picker (map picker-props (range 4) param-labels params))))))))

