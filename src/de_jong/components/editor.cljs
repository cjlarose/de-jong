(ns de-jong.components.editor
  (:require [om.core :as om]
            [om.dom :as dom]
            [de-jong.components.params-picker :refer [params-picker]]))

(defn editor [params-seq owner]
  (reify
    om/IRender
    (render [this]
      (apply dom/div #js {:className "params-picker-container"}
        (om/build-all params-picker params-seq)))))
