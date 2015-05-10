(ns de-jong.params-picker
  (:require [om.core :as om]
            [om.dom :as dom]))

(defn parse-value [e]
  (js/Number.parseFloat (.. e -target -value)))

(defn param-picker [{:keys [label value on-change]} owner]
  (reify
    om/IRender
    (render [this]
      (dom/li nil
        (dom/label
          #js {:htmlFor (str "param-" label)}
          label
          (dom/input #js {:type "range"
                          :id (str "param-" label)
                          :min -3.14
                          :max 3.14
                          :step "0.01"
                          :value value
                          :onChange (comp on-change parse-value) }))))))

(defn params-picker [{:keys [params on-change]} owner]
  (reify
    om/IRender
    (render [this]
      (let [param-labels ["α" "β" "γ" "δ"]
            picker-props (fn [i label v] { :label label
                                           :value v
                                           :on-change #(on-change (assoc params i %)) })]
        (dom/section #js {:id "params-picker"}
          (apply dom/ul nil
            (om/build-all param-picker (map picker-props (range 4) param-labels params))))))))

