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

(defn get-context []
  (let [canvas  (.getElementById js/document "canvas")
        context (.getContext canvas "2d")
        w       (.-width canvas)
        h       (.-height canvas)]
    { :size [w, h] :context context }))

(defonce my-context (do (get-context)))

(defn render-ifs [ifs num-points]
  (let [all-points  (take num-points (iterate ifs [0 0]))
        {[w h] :size ctx :context} my-context
        image-data (.createImageData ctx w h)
        data (.-data image-data)
        get-index (fn [[x y]]
                    (* 4 (+ (* w (.floor js/Math y)) (.floor js/Math x))))
        get-color (fn [[x y]]
                    (let [idx (get-index [x y])]
                      [(aget data idx)
                       (aget data (+ 1 idx))
                       (aget data (+ 2 idx))
                       (aget data (+ 3 idx))]))
        set-color (fn [[x y] [r g b a]]
                    (let [idx (get-index [x y])]
                      (aset data idx r)
                      (aset data (+ 1 idx) g)
                      (aset data (+ 2 idx) b)
                      (aset data (+ 3 idx) a)))
        blend-colors (fn [mode [rx gx bx ax] [ry gy by ay]]
                       (let [apply-mode (fn [a b] (.floor js/Math (* 255 (mode (/ a 255) (/ b 255)))))]
                           ;; (println (str "blending " [rx gx gx ax] " and " [ry gy by ay] " into " 
                           ;; [(apply-mode rx ry) (apply-mode gx gy) (apply-mode bx by) (apply-mode ax ay)])
                           ;; )
                           [(apply-mode rx ry) (apply-mode gx gy) (apply-mode bx by) (apply-mode ax ay)]))
        add-color (fn [pos color]
                    (set-color pos (blend-colors (fn [a b] (- 1 (* (- 1 a) (- 1 b)))) (get-color pos) color)))
        fill-image-data (fn [points]
                           (doseq [[x y] points]
                             ;;(println (str "setting " (* (+ x 2) 200) ", " (* (+ y 2) 200)))
                             (add-color [(* (+ x 2) 200) (* (+ y 2) 200)] [0 192 0 64])))]
    (fill-image-data all-points)
    (.putImageData ctx image-data 0 0)))

;; (def my-ifs (hello/de-jong-ifs 0.97 -1.9 1.38 -1.5))
;; (hello/render-ifs my-ifs 10)

(def my-ifs (de-jong-ifs 0.97 -1.9 1.38 -1.5))
(render-ifs my-ifs 1e5)
