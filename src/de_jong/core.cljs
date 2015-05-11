(ns ^:figwheel-always de-jong.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.browser.repl :as repl]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [chan >!]]
            [de-jong.params-picker :refer [params-picker]]
            [de-jong.ifs-viewer :refer [ifs-viewer]]
            [de-jong.points-calculator :refer [points-to-draw random-points de-jong-ifs]]))

(enable-console-print!)

(defonce app-state (atom {:ifs-params [0.97 -1.9 1.38 -1.5]}))

(defn de-jong-app [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:should-randomize false})
    om/IWillMount
    (will-mount [_]
      (let [rand-points #(take points-to-draw (random-points -2.0 2.0))
            points      (atom (rand-points))
            draw-chan   (chan)]
        (om/set-state! owner :draw-chan draw-chan)
        (go (while true
              (>! draw-chan @points)
              (let [params (om/get-props owner :ifs-params)
                    ifs    (apply de-jong-ifs params)
                    randomize (om/get-state owner :should-randomize)]
                (swap! points (if randomize
                                (fn [_]
                                  (om/set-state! owner :should-randomize false)
                                  (rand-points))
                                (partial map ifs))))))))
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
