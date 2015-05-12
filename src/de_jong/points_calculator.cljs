(ns de-jong.points-calculator)

(def points-to-draw (js/Math.pow 2 13))

(defn de-jong-ifs [a b c d]
  {:pre [(every? #(and (<= % js/Math.PI) (>= % (- js/Math.PI))) [a b c d])]}
  (fn [x y z]
    [(- (js/Math.sin (* a y)) (js/Math.cos (* b x)))
     (- (js/Math.sin (* c x)) (js/Math.cos (* d y)))
     (- (js/Math.sin (* 2.0 x)) (js/Math.cos (* 2.0 y)))]))

(defn vertex-array [length]
  (js/Float32Array. (* 3 length)))

(defn mutate-in-place! [f vtex-arr]
  (doseq [i (range 0 (.-length vtex-arr) 3)]
    (let [x (aget vtex-arr i)
          y (aget vtex-arr (+ i 1))
          z (aget vtex-arr (+ i 2))
          [x2 y2 z2] (f x y z)]
      (aset vtex-arr i x2)
      (aset vtex-arr (+ i 1) y2)
      (aset vtex-arr (+ i 2) z2)))
  vtex-arr)

(defn random-vals [minimum maximum]
  (repeatedly #(+ (rand (- maximum minimum)) minimum)))

(defn write-random-values! [vtex-arr minimum maximum]
  (let [length (.-length vtex-arr)]
    (loop [i      0
           values (random-vals minimum maximum)]
      (if (< i length)
        (do
          (aset vtex-arr i (first values))
          (recur (inc i) (rest values)))))))
