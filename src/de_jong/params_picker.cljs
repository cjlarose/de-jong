(ns de-jong.params-picker
  (:require [om.core :as om]
            [om.dom :as dom]))

(defn param-picker [{:keys [label value onChange]} owner]
  (reify
    om/IRender
    (render [this]
      (dom/li nil
        (dom/label
          #js {:htmlFor (str "param-" label)}
          label
          (dom/input #js {:type "range"
                          :name (str "param-" label)
                          :min -3.14
                          :max 3.14
                          :step "0.01"
                          :value value
                          :onChange (fn [e] (onChange (.parseFloat js/Number (.. e -target -value))))}))))))

(defn params-picker [{:keys [params onChange]} owner]
  (reify
    om/IRender
    (render [this]
      (let [param-labels {:a "α" :b "β" :c "γ" :d "δ"}
            picker-props (fn [k] {:label (k param-labels)
                                  :value (k params)
                                  :onChange #(onChange (assoc params k %))})]
        (println params)
        (dom/section #js {:id "params-picker"}
          (apply dom/ul nil
            (om/build-all param-picker (map picker-props [:a :b :c :d]))))))))

