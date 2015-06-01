(ns de-jong.components.editor
  (:require [om.core :as om]
            [om.dom :as dom]
            [de-jong.points-calculator :refer [random-vals]]
            [de-jong.components.fa-icon :refer [fa-icon]]
            [de-jong.components.params-picker :refer [params-picker]]
            [de-jong.components.point-cloud :refer [point-cloud]]))

(defn preview [params]
  (dom/div #js { :className "preview" }
    (om/build point-cloud { :num-points (js/Math.pow 2 13)
                            :de-jong-params params
                            :point-size 0.5
                            :width 250
                            :height 166 })))

(defn remove-button-container [params idx]
  (let [remove-frame (fn [e]
                       (.preventDefault e)
                       (om/transact! params (fn [p] (vec (concat (subvec p 0 idx) (subvec p (inc idx)))))))]
    (dom/a #js { :href "#"
                 :className "remove-frame"
                 :onClick remove-frame }
      (fa-icon "times-circle"))))

(defn frame-editor [[params idx] owner]
  (reify
    om/IRender
    (render [_]
      (dom/li #js { :className "frame-editor" }
        (if (> (count params) 1) (remove-button-container params idx))
        (preview (nth params idx))
        (om/build params-picker (nth params idx))))))

(defn add-frame-container [ifs-params]
  (let [new-params (vec (take 4 (random-vals (- js/Math.PI) js/Math.PI)))
        add-frame (fn [e]
                    (.preventDefault e)
                    (om/transact! ifs-params #(conj % new-params)))]
    (dom/li #js { :className "add-frame-container" }
      (dom/a #js { :href "#" :onClick add-frame }
        (fa-icon "plus-circle")))))

(defn editor [{:keys [ifs-params show-editor]} owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className (str "editor-wrap" (if show-editor "" " hidden"))}
        (apply dom/ul #js { :className "editor"
                            :style #js { :width (str (* 250 (+ 1 (count ifs-params))) "px") } }
          (concat
            (om/build-all frame-editor (map (fn [idx] [ifs-params idx]) (range (count ifs-params))))
            [(add-frame-container ifs-params)]))))))
