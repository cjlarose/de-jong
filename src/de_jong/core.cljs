(ns ^:figwheel-always de-jong.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.browser.repl :as repl]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [chan >! close! timeout put!]]
            [de-jong.components.params-picker :refer [params-picker]]
            [de-jong.components.ifs-viewer :refer [ifs-viewer]]
            [de-jong.points-calculator :refer [points-to-draw
                                               de-jong-ifs
                                               random-vertex-array
                                               vertices-apply]]
            [de-jong.transition :refer [transition-params]]))

(enable-console-print!)

(defonce app-state (atom {:ifs-params [[1   1   -1   1]
                                       [1   2.5 -1.5 2.5]
                                       [1.5 2.5 -1   2.5]]}))

(defn animation-frame
  ([]
    (animation-frame (chan)))
  ([comm]
    (put! comm (.requestAnimationFrame js/window (fn [_] (animation-frame comm))))
    comm))

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
          (swap! points-array (partial vertices-apply ifs))
          (swap! params-seq rest)))))
    draw-chan))

(def make-transition (comp cycle (partial transition-params 120)))

(defn de-jong-app [data owner]
  (reify
    om/IInitState
    (init-state [_]
      (let [params-chan (chan)
            initial-seq (make-transition (:ifs-params data))]
        {:params-chan params-chan
         :draw-chan   (calculator-channel params-chan initial-seq)}))
    om/IWillReceiveProps
    (will-receive-props [this {:keys [ifs-params] :as next-props}]
      (let [old-ifs-params (om/get-props owner :ifs-params)]
        (if-not (= old-ifs-params ifs-params)
          (let [params-chan (om/get-state owner :params-chan)]
            (put! params-chan (make-transition ifs-params))))))
    om/IRenderState
    (render-state [this {:keys [draw-chan]}]
      (dom/div nil
        (apply dom/div #js {:className "params-picker-container"}
          (om/build-all params-picker (:ifs-params data)))
        (om/build ifs-viewer draw-chan)))))

(om/root de-jong-app app-state
  {:target (. js/document (getElementById "application"))})
