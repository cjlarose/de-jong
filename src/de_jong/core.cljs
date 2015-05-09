(ns ^:figwheel-always de-jong.core
  (:require [clojure.browser.repl :as repl]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [de-jong.params-picker :refer [params-picker]]
            [de-jong.points-calculator :refer [points-calculator]]))

(enable-console-print!)

(defonce app-state (atom {:ifs-params {:a 0.97 :b -1.9 :c 1.38 :d -1.5}}))

(defn handle-params-change [data params]
  (om/transact! data :ifs-params
    (fn [_] params)))

(defn de-jong-app [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/div nil
        (om/build params-picker {:onChange (partial handle-params-change data)
                                 :params (:ifs-params data)})
        (om/build points-calculator (:ifs-params data))))))

(om/root de-jong-app app-state
  {:target (. js/document (getElementById "application"))})
