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
          (dom/input #js {:type "number"
                          :name (str "param-" label)
                          :min -3.14
                          :max 3.14
                          :step "0.01"
                          :value value
                          :onChange (fn [e] (onChange (.-value (.-target e))))}))))))

(defn handle-param-change [owner k v]
  (om/set-state! owner k v)
  (println (str k " is now set to " v)))

(defn params-picker [{:keys [params onChange]} owner]
  (reify
    om/IInitState
    (init-state [_]
      params)
    om/IRenderState
    (render-state [this state]
      (let [param-labels {:a "α" :b "β" :c "γ" :d "δ"}
            picker-props (fn [k] {:label (k param-labels)
                                  :value (k state)
                                  :onChange (partial handle-param-change owner k)})]
        (println state)
        (dom/section #js {:id "params-picker"}
          (dom/form #js {:onSubmit (fn [e] (.preventDefault e) (onChange state))}
            (apply dom/ul nil
              (om/build-all param-picker (map picker-props [:a :b :c :d])))
            (dom/input #js {:type "submit" :value "Draw"})))))))

