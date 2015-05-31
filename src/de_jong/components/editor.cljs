(ns de-jong.components.editor
  (:require [om.core :as om]
            [om.dom :as dom]
            [de-jong.components.params-picker :refer [params-picker]]
            [de-jong.components.point-cloud :refer [point-cloud]]))

(defn preview [params]
  (dom/div #js { :className "preview" }
    (om/build point-cloud { :num-points (js/Math.pow 2 13)
                            :de-jong-params params
                            :point-size 0.5
                            :width 250
                            :height 166 })))

(defn frame-editor [params owner]
  (reify
    om/IRender
    (render [_]
      (dom/li #js { :className "frame-editor" }
        (preview params)
        (om/build params-picker params)))))

(defn editor [{:keys [ifs-params show-editor]} owner]
  (reify
    om/IRender
    (render [this]
      (apply dom/ul #js {:className (str "editor" (if show-editor "" " hidden"))}
        (om/build-all
          frame-editor
          ifs-params)))))
