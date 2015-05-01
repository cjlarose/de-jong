(ns hello-world.core
  (:require [clojure.browser.repl :as repl]))

(defonce conn
  (repl/connect "http://localhost:9000/repl")) 

(enable-console-print!)

(println "Hello world!")

(defn foo [a b]
  (* a b))
