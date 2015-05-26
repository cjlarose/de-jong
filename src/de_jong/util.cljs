(ns de-jong.util
  (:require [cljs.core.async :refer [chan put!]]))

(defn animation-frame
  ([]
    (animation-frame (chan)))
  ([comm]
    (put! comm (.requestAnimationFrame js/window (fn [_] (animation-frame comm))))
    comm))

