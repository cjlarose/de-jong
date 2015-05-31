(ns ^:figwheel-always de-jong.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.browser.repl :as repl]
            [cljs.core.async :refer [chan <! put! timeout sliding-buffer]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [de-jong.query-params :refer [extract-app-state
                                          update-history!
                                          popstate-changes]]
            [de-jong.components.app :refer [app]]))

(enable-console-print!)

(def default-state
  { :ifs-params  [[-2.950 1     -1     1]
                  [-2.850 2.793 -2.697 1.128]
                  [1.5    2.5   0.731  2.5]]
    :show-editor true })

(defonce app-state
  (let [qs (.-search (.-location js/window))
        qs-state (extract-app-state qs)
        init-state (if qs-state qs-state default-state)]
    (update-history! init-state true)
    (atom init-state)))

(defn root [app-state owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (let [comm (popstate-changes)]
        (go (while true
          (let [{:keys [state]} (<! comm)]
            (if state
              (om/transact! app-state nil (constantly state) :popped)))))))
    om/IRender
    (render [this]
      (om/build app app-state))))

(defn throttle
  "https://gist.github.com/swannodette/5886048"
  [c ms]
  (let [c' (chan)]
    (go (while true
      (>! c' (<! c))
      (<! (timeout ms))))
    c'))

(def tx-chan (chan (sliding-buffer 1)))

(let [c (throttle tx-chan 500)]
  (go (while true
    (let [[{ :keys [new-state tag] } _] (<! c)]
           (if-not (= tag :popped)
             (update-history! new-state))))))

(om/root root app-state
  { :target (. js/document (getElementById "application"))
    :tx-listen (fn [a b] (put! tx-chan [a b])) })
