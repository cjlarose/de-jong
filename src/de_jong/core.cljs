(ns ^:figwheel-always de-jong.core
  (:require [clojure.browser.repl :as repl]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [de-jong.components.app :refer [app]]))

(enable-console-print!)

(defonce app-state (atom { :ifs-params [[-2.950 1     -1     1]
                                        [-2.850 2.793 -2.697 1.128]
                                        [1.5    2.5   0.731  2.5]]
                           :selection {:idx 0} }))


(om/root app app-state
  {:target (. js/document (getElementById "application"))})
