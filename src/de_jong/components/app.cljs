(ns de-jong.components.app
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [chan >! put!]]
            [de-jong.util :refer [animation-frame]]
            [de-jong.components.point-cloud :refer [full-screen-point-cloud]]
            [de-jong.components.editor :refer [editor]]
            [de-jong.components.params-picker :refer [params-picker]]
            [de-jong.points-calculator :refer [points-to-draw
                                               de-jong-ifs
                                               random-vertex-array
                                               vertices-apply]]
            [de-jong.transition :refer [transition-params]]))
 
(defn calculator-channel [params-chan initial-seq]
  (let [throttler    (animation-frame)
        draw-chan    (chan)
        random-array (random-vertex-array points-to-draw -2.0 2.0)
        points-array (atom random-array)
        params-seq   (atom initial-seq)]
    (go (while true
      (>! draw-chan @points-array)
      (let [[v port] (alts! [params-chan throttler])]
        (if (= port params-chan)
          (do
            (reset! params-seq v)
            (reset! points-array random-array)))
        (let [ifs (apply de-jong-ifs (first @params-seq))]
          (reset! points-array random-array)
          (swap! points-array (partial vertices-apply ifs))
          (swap! params-seq rest)))))
    draw-chan))

(def make-transition (comp cycle (partial transition-params 120)))
;;(def make-transition (repeat (first transition-params)))

(defn app [{:keys [selection ifs-params] :as data} owner]
  (reify
    om/IInitState
    (init-state [_]
      (let [params-chan (chan)
            initial-seq (repeat (first ifs-params))]
            ;;initial-seq (make-transition ifs-params)]
        {:params-chan params-chan
         :draw-chan   (calculator-channel params-chan initial-seq)}))
    om/IWillReceiveProps
    (will-receive-props [this {:keys [ifs-params] :as next-props}]
      (let [old-ifs-params (om/get-props owner :ifs-params)]
        (if-not (= old-ifs-params ifs-params)
          (let [params-chan (om/get-state owner :params-chan)]
            (put! params-chan (repeat (first ifs-params)))))))
            ;;(put! params-chan (make-transition ifs-params))))))
    om/IRenderState
    (render-state [this {:keys [draw-chan]}]
      (dom/div nil
        (om/build editor data)
        (dom/div #js {:className "params-picker-container"}
          (om/build params-picker (nth ifs-params (:idx selection))))
        (om/build full-screen-point-cloud draw-chan)))))
