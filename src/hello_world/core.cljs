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

(defn get-index [[w h] [x y]]
            (* 4 (+ (* w (.floor js/Math y)) (.floor js/Math x))))

(defn get-color [{size :size data :data} [x y]]
  (let [idx (get-index size [x y])]
    [(aget data idx)
     (aget data (+ 1 idx))
     (aget data (+ 2 idx))
     (aget data (+ 3 idx))]))

(defn set-color [{size :size data :data} [x y] [r g b a]]
  (let [idx (get-index size [x y])]
    (aset data idx r)
    (aset data (+ 1 idx) g)
    (aset data (+ 2 idx) b)
    (aset data (+ 3 idx) a)))

(defn blend-colors [mode [rx gx bx ax] [ry gy by ay]]
  (let [apply-mode (fn [a b] (.floor js/Math (* 255 (mode (/ a 255) (/ b 255)))))]
    [(apply-mode rx ry) (apply-mode gx gy) (apply-mode bx by) (apply-mode ax ay)]))

(defn screen [a b]
  (- 1 (* (- 1 a) (- 1 b))))

(defn render-ifs [ifs num-points]
  (let [all-points  (take num-points (iterate ifs [0 0]))
        {[w h] :size ctx :context} my-context
        fill-image-data (fn [points]
          (let [image-data (.createImageData ctx w h)
                data-with-size {:data (.-data image-data) :size [w h]}
                add-color (fn [pos color]
                            (set-color
                              data-with-size
                              pos
                              (blend-colors screen (get-color data-with-size pos) color)))]
            (doseq [[x y] points]
              (add-color [(* (+ x 2) 200) (* (+ y 2) 200)] [0 192 0 64]))
            image-data))]
    (.putImageData ctx (fill-image-data all-points) 0 0)))

;; (def my-ifs (hello/de-jong-ifs 0.97 -1.9 1.38 -1.5))
;; (hello/render-ifs my-ifs 10)

(def my-ifs (de-jong-ifs 0.97 -1.9 1.38 -1.5))
(render-ifs my-ifs 1e5)
