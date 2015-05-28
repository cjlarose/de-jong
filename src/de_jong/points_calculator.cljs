(ns de-jong.points-calculator)

(def points-to-draw (js/Math.pow 2 13))

(defn in-range [x]
  (and (>= x (- js/Math.PI)) (<= x js/Math.PI)))

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
