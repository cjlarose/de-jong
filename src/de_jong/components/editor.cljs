(ns de-jong.components.editor
  (:require [om.core :as om]
            [om.dom :as dom]
            [cljs.core.async :refer [chan put!]]
            [de-jong.components.point-cloud :refer [point-cloud]]))

(defn preview [{:keys [params onSelect selected]} owner]
  (reify
    om/IInitState
    (init-state [_]
      { :draw-chan (chan) })
    om/IDidMount
    (did-mount [_]
      (put! (om/get-state owner :draw-chan) params))
    om/IWillReceiveProps
    (will-receive-props [this next-props]
      (if-not (= params (:params next-props))
        (put! (om/get-state owner :draw-chan) (:params next-props))))
    om/IRenderState
    (render-state [this { :keys [draw-chan] }]
      (dom/li #js {:className (str "preview" (if selected " selected"))}
        (dom/a #js {:href "#" :onClick (fn [e] (.preventDefault e) (onSelect))}
          (om/build point-cloud { :draw-chan draw-chan :point-size 0.25 :width 150 :height 100 }))))))

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
