(ns de-jong.core
  (:require [clojure.browser.repl :as repl]
            [om.core :as om]
            [om.dom :as dom]
            [de-jong.params-picker :refer [params-picker]]
            [de-jong.ifs-viewer :refer [ifs-viewer]]))

;; (defonce conn
;;   (repl/connect "http://localhost:9000/repl"))

(enable-console-print!)

(defonce app-state (atom {:ifs-params {:a 0.97 :b -1.9 :c 1.38 :d -1.5}}))

(defn handle-params-change [params]
  (println "yo")
  (println params))

(defn de-jong-app [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/div nil
        (om/build params-picker {:onChange handle-params-change
                                 :params (:ifs-params data)})
        (om/build ifs-viewer (:ifs-params data))))))

(om/root de-jong-app app-state
  {:target (. js/document (getElementById "application"))})
