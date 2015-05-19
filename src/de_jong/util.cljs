(ns de-jong.util)

(defn write-values! [arr vertices-seq]
  (let [length (.-length arr)]
    (loop [i      0
           values vertices-seq]
      (if (< i length)
        (do
          (let [[x y z] (first values)]
            (aset arr i x)
            (aset arr (+ i 1) y)
            (aset arr (+ i 2) z))
          (recur (+ i 3) (rest values)))))))
