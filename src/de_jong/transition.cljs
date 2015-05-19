(ns de-jong.transition
  (:require [de-jong.points-calculator :refer [in-range]]))

(defn lerp [t initial final]
  {:pre [(and (>= t 0) (<= t 1))]}
  (+ initial (* t (- final initial))))

(defn lerp-vectors [num-frames initial final]
  (let [step     (/ 1 num-frames)
        vec-lerp (fn [t i f] (map (partial lerp t) i f))]
    (map #(vec-lerp % initial final) (range 0 1 step))))

(defn transition-params
  "For a sequence of de jong parameters
  ((a_0 b_0 c_0 d_0) (a_1 b_1 c_1 d_1) (a_2 b_2 c_2 d_2) ...), and a number of
  frames, returns another sequence of de jong paramters with num-frames frames
  interposed
  ((a_0 b_0 c_0 d_0) {num-frames more frames} (a_1 b_1 c_1 d_1)
  {num-frame more frames} (a_2 b_2 c_2 d_2) ...)"
  [num-frames xs]
  (let [pairs (partition 2 1 xs)]
    (mapcat (fn [[a b]] (lerp-vectors num-frames a b)) pairs)))
