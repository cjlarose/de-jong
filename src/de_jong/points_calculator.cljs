(ns de-jong.points-calculator
  (:require [de-jong.util :refer [write-values!]]))

(defn in-range [x]
  (and (>= x (- js/Math.PI)) (<= x js/Math.PI)))

(defn de-jong-ifs [a b c d]
  {:pre [(every? in-range [a b c d])]}
  (fn [x y _]
    [(- (js/Math.sin (* a y)) (js/Math.cos (* b x)))
     (- (js/Math.sin (* c x)) (js/Math.cos (* d y)))
     0]))

(defn- vertex-array [length]
  (js/Float32Array. (* 3 length)))

(defn lattice-vertex-array [length minimum maximum]
  (let [arr  (vertex-array length)
        size (.sqrt js/Math length)
        ratio (/ (- maximum minimum) size)
        vertices (for [x (range size) y (range size)]
                         [(+ (* x ratio) minimum) (+ (* y ratio) minimum) 0])]
    (write-values! arr vertices)
    arr))

(defn vertices-apply [f vertices]
  (let [length  (.-length vertices)
        new-arr (vertex-array (/ length 3))]
    (loop [i 0]
      (if (< i length)
        (let [x (aget vertices i)
              y (aget vertices (+ i 1))
              z (aget vertices (+ i 2))
              [x2 y2 z2] (f x y z)]
          (aset new-arr i x2)
          (aset new-arr (+ i 1) y2)
          (aset new-arr (+ i 2) z2)
          (recur (+ i 3) ))))
    new-arr))
