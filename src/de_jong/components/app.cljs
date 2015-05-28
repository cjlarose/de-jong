(ns de-jong.components.app
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [chan >! put!]]
            [de-jong.util :refer [animation-frame]]
            [de-jong.components.point-cloud :refer [full-screen-point-cloud]]
            [de-jong.components.editor :refer [editor]]
            [de-jong.components.params-picker :refer [params-picker]]
            [de-jong.transition :refer [transition-params]]))
 
(defn calculator-channel [scene-chan initial-seq]
  (let [throttler    (animation-frame)
        params-chan  (chan)
        params-seq   (atom initial-seq)]
    (go (while true
      (>! params-chan (first @params-seq))
      (let [[v port] (alts! [scene-chan throttler])]
        (if (= port scene-chan)
          (reset! params-seq v)
          (swap! params-seq rest)))))
    params-chan))

(def make-transition (comp cycle (partial transition-params 120)))

(defn app [{:keys [selection ifs-params] :as data} owner]
  (reify
    om/IInitState
    (init-state [_]
      (let [scene-chan (chan)
            initial-seq (make-transition ifs-params)]
        {:scene-chan scene-chan
         :draw-chan   (calculator-channel scene-chan initial-seq)}))
    om/IWillReceiveProps
    (will-receive-props [this {:keys [ifs-params] :as next-props}]
      (let [old-ifs-params (om/get-props owner :ifs-params)]
        (if-not (= old-ifs-params ifs-params)
          (let [scene-chan (om/get-state owner :scene-chan)]
            (put! scene-chan (make-transition ifs-params))))))
    om/IRenderState
    (render-state [this {:keys [draw-chan]}]
      (dom/div nil
        (om/build editor data)
        (dom/div #js {:className "params-picker-container"}
          (om/build params-picker (nth ifs-params (:idx selection))))
        (om/build full-screen-point-cloud [draw-chan (js/Math.pow 2 13)])))))
