(ns ^:figwheel-always de-jong.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.browser.repl :as repl]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [chan >!]]
            [de-jong.params-picker :refer [params-picker]]
            [de-jong.ifs-viewer :refer [ifs-viewer]]
            [de-jong.points-calculator :refer [points-to-draw random-points
                                               de-jong-ifs vertex-array
                                               mutate-in-place!]]))

(enable-console-print!)

(defonce app-state (atom {:ifs-params [0.97 -1.9 1.38 -1.5]}))

(defn de-jong-app [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:should-randomize false})
    om/IWillMount
    (will-mount [_]
      (let [points-array  (vertex-array points-to-draw)
            rand-mutation (fn [_ _ _] (first (random-points -2.0 2.0)))
            draw-chan     (chan)]
        (mutate-in-place! rand-mutation points-array)
        (om/set-state! owner :draw-chan draw-chan)
        (go (while true
              (>! draw-chan points-array)
              (let [params (om/get-props owner :ifs-params)
                    ifs    (apply de-jong-ifs params)
                    randomize (om/get-state owner :should-randomize)]
                (mutate-in-place! (if randomize
                                    (comp (partial apply ifs) rand-mutation)
                                    ifs)
                                  points-array)
                (if randomize (om/set-state! owner :should-randomize false)))))))
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
