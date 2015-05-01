(ns hello-world.core
  (:require [clojure.browser.repl :as repl]))

(defonce conn
  (repl/connect "http://localhost:9000/repl")) 

(enable-console-print!)

(println "Hello world!")

(defn foo [a b]
  (* a b))

(defn de-jong-ifs [a b c d]
  {:pre [(every? #(and (<= % js/Math.PI) (>= % (- js/Math.PI))) [a b c d])]}
  (fn [[x y]]
    [(- (js/Math.sin (* a y)) (js/Math.cos (* b x)))
     (- (js/Math.sin (* c x)) (js/Math.cos (* d y)))]))

;; to get a lazy seq of points use
;; (iterate (hello/de-jong-ifs 0.97 -1.9 1.38 -1.5) [0 0])

(defn render-ifs [ifs num-points]
  (let [points  (take num-points (iterate ifs [0 0])),
        canvas  (.getElementById js/document "canvas")
        context (.getContext canvas "2d")
        w       (.-width canvas)
        h       (.-height canvas)]
    (.clearRect context 0 0 w h)
    (.save context)
    (.translate context (/ w 2) (/ h 2))
    (.scale context (/ w 4) (/ h 4))
    (set! (.-fillStyle context) "green")
    (doseq [[x y] points] (.fillRect context x y 1e-3 1e-3))
    (.restore context)))

;; (def my-ifs (hello/de-jong-ifs 0.97 -1.9 1.38 -1.5))
;; (hello/render-ifs my-ifs 10)

(def my-ifs (de-jong-ifs 0.97 -1.9 1.38 -1.5))
(render-ifs my-ifs 1e5)
