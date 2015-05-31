(ns de-jong.components.editor
  (:require [om.core :as om]
            [om.dom :as dom]
            [de-jong.components.params-picker :refer [params-picker]]
            [de-jong.components.point-cloud :refer [point-cloud]]))

(defn preview [params onSelect]
  (dom/div #js {:className "preview"}
    (dom/a #js {:href "#" :onClick (fn [e] (.preventDefault e) (onSelect))}
      (om/build point-cloud { :num-points (js/Math.pow 2 13)
                              :de-jong-params params
                              :point-size 0.5
                              :width 250
                              :height 166 }))))

(defn frame-editor [{:keys [params onSelect selected]} owner]
  (reify
    om/IRender
    (render [_]
      (dom/li #js {:className (str "frame-editor" (if selected " selected"))}
        (preview params onSelect)
        (if selected (om/build params-picker params)) 
        ))))

(defn frame-editor-params [selection idx params]
  { :onSelect (fn [] (println idx) (om/transact! selection #(assoc % :idx idx)))
    :selected (= idx (:idx selection))
    :params   params })

(defn editor [{:keys [ifs-params selection]} owner]
  (reify
    om/IRender
    (render [this]
      (dom/div nil
        (apply dom/ul #js {:className "editor"}
          (om/build-all
            frame-editor
            (map-indexed (partial frame-editor-params selection) ifs-params)))))))
