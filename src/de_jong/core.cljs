(ns ^:figwheel-always de-jong.core
  (:require [om.core :as om :include-macros true]
            [de-jong.components.app :refer [app]]))

(enable-console-print!)

(def default-state
  { :ifs-params  [[-2.950 1     -1     1]
                  [-2.850 2.793 -2.697 1.128]
                  [1.5    2.5   0.731  2.5]]
    :show-editor true })

(defonce app-state
  (atom default-state))

(defn root [app-state owner]
  (reify
    om/IRender
    (render [this]
      (om/build app app-state))))

(om/root root app-state
  { :target (. js/document (getElementById "application")) })
