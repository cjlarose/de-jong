(ns de-jong.components.top-bar
  (:require [om.core :as om]
            [om.dom :as dom]))

(defn editor-link [app-state]
  (dom/a 
    #js { :href "#"
          :onClick (fn [e]
                     (.preventDefault e)
                     (om/transact!
                       app-state
                       (fn [s] (-> s
                                  (assoc :show-editor (not (:show-editor s)))
                                  (assoc :selection { :idx nil } ))))) }
    (if (:show-editor app-state) "Hide Edtior" "Show Editor")))

(defn top-bar [app-state owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "top-bar"} (editor-link app-state)))))
