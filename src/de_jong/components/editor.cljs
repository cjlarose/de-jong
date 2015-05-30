(ns de-jong.components.editor
  (:require [om.core :as om]
            [om.dom :as dom]
            [de-jong.components.point-cloud :refer [point-cloud]]))

(defn preview [{:keys [params onSelect selected]} owner]
  (reify
    om/IRender
    (render [_]
      (dom/li #js {:className (str "preview" (if selected " selected"))}
        (dom/a #js {:href "#" :onClick (fn [e] (.preventDefault e) (onSelect))}
          (om/build point-cloud { :num-points (js/Math.pow 2 13)
                                  :de-jong-params params
                                  :point-size 0.5
                                  :width 150
                                  :height 100 }))))))

(defn preview-params [selection idx params]
  { :onSelect (fn [] (om/transact! selection #(assoc % :idx idx)))
    :selected (= idx (:idx selection))
    :params   params })

(defn editor [{:keys [ifs-params selection]} owner]
  (reify
    om/IRender
    (render [this]
      (dom/div nil
        (apply dom/ul #js {:className "editor"}
          (om/build-all
            preview
            (map-indexed (partial preview-params selection) ifs-params)))))))
