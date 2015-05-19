(ns de-jong.points-calculator)

(def points-to-draw (js/Math.pow 2 13))

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

(defn random-vals [minimum maximum]
  (repeatedly #(+ (rand (- maximum minimum)) minimum)))

(defn- write-random-values! [minimum maximum vertices]
  (let [length (.-length vertices)]
    (loop [i      0
           values (random-vals minimum maximum)]
      (if (< i length)
        (do
          (aset vertices i (first values))
          (recur (inc i) (rest values)))))
    vertices))

(defn random-vertex-array [length minimum maximum]
  (let [arr (vertex-array length)]
    (write-random-values! minimum maximum arr)))

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
