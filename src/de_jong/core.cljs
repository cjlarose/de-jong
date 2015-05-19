(ns ^:figwheel-always de-jong.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.browser.repl :as repl]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [chan >! close! timeout put!]]
            [de-jong.components.params-picker :refer [params-picker]]
            [de-jong.components.ifs-viewer :refer [ifs-viewer]]
            [de-jong.constants :refer [points-to-draw]]
            [de-jong.points-calculator :refer [de-jong-ifs
                                               lattice-vertex-array
                                               vertices-apply]]))

(enable-console-print!)

(defonce app-state (atom {:ifs-params [0.97 -1.9 1.38 -1.5]}))

(defn animation-frame
  ([]
    (animation-frame (chan)))
  ([comm]
    (put! comm (.requestAnimationFrame js/window (fn [_] (animation-frame comm))))
    comm))

(defn calculator-channel [params-chan initial-seq]
  (let [throttler     (animation-frame)
        draw-chan     (chan)
        initial-array (lattice-vertex-array points-to-draw -2.0 2.0)
        points-array  (atom initial-array)
        params-seq    (atom initial-seq)]
    (go (while true
      (>! draw-chan @points-array)
      (let [[v port] (alts! [params-chan throttler])]
        (if (= port params-chan)
          (do
            (reset! params-seq v)
            (reset! points-array initial-array)
            (let [ifs (apply de-jong-ifs (first @params-seq))]
              (swap! points-array (partial vertices-apply ifs))
              (swap! params-seq rest)))))))
    draw-chan))

(defn de-jong-app [data owner]
  (reify
    om/IInitState
    (init-state [_]
      (let [params-chan (chan)]
        {:params-chan params-chan
         :draw-chan   (calculator-channel params-chan (repeat (:ifs-params data)))}))
    om/IWillReceiveProps
    (will-receive-props [this {:keys [ifs-params] :as next-props}]
      (let [old-ifs-params (om/get-props owner :ifs-params)]
        (if-not (= old-ifs-params ifs-params)
          (let [params-chan (om/get-state owner :params-chan)]
            (put! params-chan (repeat ifs-params))))))
    om/IRenderState
    (render-state [this {:keys [draw-chan]}]
      (dom/div nil
        (om/build params-picker (:ifs-params data))
        (om/build ifs-viewer draw-chan)))))

(om/root de-jong-app app-state
  {:target (. js/document (getElementById "application"))})
