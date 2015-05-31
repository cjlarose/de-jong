(ns de-jong.components.top-bar
  (:require [om.core :as om]
            [om.dom :as dom]))

(defn fa-icon [icon]
  (dom/i #js { :className (str "fa fa-" icon) } ))

(defn editor-link [app-state]
  (dom/a 
    #js { :href "#"
          :className (str "toggle-editor" (if (:show-editor app-state) " active"))
          :onClick (fn [e]
                     (.preventDefault e)
                     (om/transact!
                       app-state
                       (fn [s] (-> s
                                  (assoc :show-editor (not (:show-editor s)))
                                  (assoc :selection { :idx nil } ))))) }
    (fa-icon "pencil")))

(defn top-bar [app-state owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "top-bar"} (editor-link app-state)))))
