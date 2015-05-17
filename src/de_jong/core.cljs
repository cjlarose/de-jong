(ns ^:figwheel-always de-jong.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.browser.repl :as repl]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [chan >! close!]]
            [de-jong.components.params-picker :refer [params-picker]]
            [de-jong.components.ifs-viewer :refer [ifs-viewer]]
            [de-jong.points-calculator :refer [points-to-draw
                                               de-jong-ifs
                                               random-vertex-array
                                               vertices-apply]]))

(enable-console-print!)

(defonce app-state (atom {:ifs-params [0.97 -1.9 1.38 -1.5]}))

(defn calculator-channel [owner draw-chan]
  (let [random-array (random-vertex-array points-to-draw -2.0 2.0)
        points-array (atom random-array)]
    (go (while true
      (>! draw-chan @points-array)
      (let [params (om/get-props owner :ifs-params)
            ifs    (apply de-jong-ifs params)]
        (if (om/get-state owner :should-randomize)
          (do
            (reset! points-array random-array)
            (om/set-state! owner :should-randomize false)))
        (swap! points-array (partial vertices-apply ifs)))))))

(defn de-jong-app [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:should-randomize false})
    om/IWillMount
    (will-mount [_]
      (let [draw-chan (chan)
            calc-chan (calculator-channel owner draw-chan)]
        (om/set-state! owner :draw-chan draw-chan)
        (om/set-state! owner :calc-chan calc-chan)))
    ; om/IWillUnmount
    ; (will-unmount [_]
    ;   (close! (om/get-state owner :calc-chan)))
    om/IWillReceiveProps
    (will-receive-props [this {:keys [ifs-params] :as next-props}]
      (let [old-ifs-params (om/get-props owner :ifs-params)]
        (if-not (= old-ifs-params ifs-params)
          (om/set-state! owner :should-randomize true))))
    om/IRenderState
    (render-state [this {:keys [draw-chan]}]
      (dom/div nil
        (om/build params-picker (:ifs-params data))
        (om/build ifs-viewer draw-chan)))))

(om/root de-jong-app app-state
  {:target (. js/document (getElementById "application"))})
