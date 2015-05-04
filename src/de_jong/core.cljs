(ns ^:figwheel-always de-jong.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.browser.repl :as repl]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [de-jong.params-picker :refer [params-picker]]
            [de-jong.ifs-viewer :refer [ifs-viewer]]
            [cljs.core.async :refer [chan <!]]))

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
        (om/build ifs-viewer data)))))

(om/root de-jong-app app-state
  {:target (. js/document (getElementById "application"))})
