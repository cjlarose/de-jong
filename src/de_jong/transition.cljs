(ns de-jong.transition)

(defn lerp [t initial final]
  {:pre [(and (>= t 0) (<= t 1))]}
  (+ initial (* t (- final initial))))

(defn interpolate-param-pair [xs ys]
  (fn [t]
    (map (fn [x y] (lerp t x y)) xs ys)))

(defn transition-params-fn
  "For a sequence of de jong parameters
  ((a_0 b_0 c_0 d_0) (a_1 b_1 c_1 d_1)
  (a_2 b_2 c_2 d_2) ... (a_n b_n c_n d_n)), returns a function of
  t, s.t. for inputs from 0 to 1, t returns a set of de jong
  params interpolated between the inputs"
  [xs]
  (let [pairs (partition 2 1 (conj xs (first xs)))
        fns   (map (fn [[a b]] (interpolate-param-pair a b)) pairs)]
    (fn [t]
      (let [fi (mod (Math/floor (* t (count fns))) (count fns))
            f  (nth fns fi)
            t2 (* (mod t (/ (count fns))) (count fns))]
        (f t2)))))
