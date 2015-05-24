(ns de-jong.components.editor
  (:require [om.core :as om]
            [om.dom :as dom]
            [de-jong.components.params-picker :refer [params-picker]]))

(defn preview [{:keys [params onSelect selected]} owner]
  (reify
    om/IRender
    (render [this]
      (dom/li #js {:className (str "preview" (if selected " selected"))}
        (dom/a #js {:href "#" :onClick (fn [e] (.preventDefault e) (onSelect))}
          (apply str (interpose "," params)))))))

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
          (om/build-all preview (map-indexed
                                  (partial preview-params selection)
                                  ifs-params)))
        (apply dom/div #js {:className "params-picker-container"}
          (om/build-all params-picker ifs-params))))))
