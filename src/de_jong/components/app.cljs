(ns de-jong.components.app
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [de-jong.components.point-cloud :refer [full-screen-point-cloud]]
            [de-jong.components.top-bar :refer [top-bar]]
            [de-jong.components.editor :refer [editor]]
            [de-jong.transition :refer [transition-params-fn]]))

(defn advance-frame! [owner]
  (let [{ :keys [frames frame-number] } (om/get-state owner)]
    (om/set-state! owner :frame-number (mod (inc frame-number) (count frames)))))

(defn app [{:keys [ifs-params] :as data} owner]
  (reify
    om/IInitState
    (init-state [_]
      { :param-gen (transition-params-fn ifs-params)
        :frame-number 0 })
    om/IWillReceiveProps
    (will-receive-props [this {:keys [ifs-params] :as next-props}]
      (let [old-ifs-params (om/get-props owner :ifs-params)]
        (if-not (= old-ifs-params ifs-params)
          (let [new-param-gen (transition-params-fn ifs-params)]
            (om/set-state! owner { :param-gen new-param-gen })))))
    om/IDidMount
    (did-mount [_]
      (advance-frame! owner))
    om/IDidUpdate
     (did-update [_ _ _]
       (advance-frame! owner))
    om/IRenderState
    (render-state [this {:keys [param-gen]}]
      (let [t (/ (.now (.-performance js/window)) (* 2000 (count ifs-params)))]
        (dom/div nil
          (dom/header nil
            (om/build top-bar data)
            (om/build editor data))
          (om/build full-screen-point-cloud [(param-gen t) (js/Math.pow 2 18)]))))))
