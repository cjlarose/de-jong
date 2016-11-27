(ns de-jong.transition)

(defn lerp [t initial final]
  {:pre [(and (>= t 0) (<= t 1))]}
  (+ initial (* t (- final initial))))

(defn lerp-vectors [num-frames initial final]
  (let [step     (/ 1 num-frames)
        vec-lerp (fn [t i f] (map (partial lerp t) i f))]
    (map #(vec-lerp % initial final) (range 0 1 step))))

(defn transition-params
  "For a sequence of de jong parameters
  ((a_0 b_0 c_0 d_0) (a_1 b_1 c_1 d_1)
  (a_2 b_2 c_2 d_2) ... (a_n b_n c_n d_n)), and a
  number of frames, returns another sequence of de jong
  paramters with num-frames frames interposed
  (v_0 {num-frames more frames}
   v_1 {num-frames more frames}
   v_2 {num-frames more frames}
   ...
   v_n {num-frames more frames})"
  [num-frames xs]
  (let [pairs (partition 2 1 (conj xs (first xs)))]
    (mapcat (fn [[a b]] (lerp-vectors num-frames a b)) pairs)))
