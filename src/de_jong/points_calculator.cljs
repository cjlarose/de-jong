(ns de-jong.points-calculator
  (:require [om.core :as om]
            [om.dom :as dom]
            [de-jong.ifs-viewer :refer [ifs-viewer]]))

(def points-to-draw (js/Math.pow 2 13))

(defn de-jong-ifs [a b c d]
  {:pre [(every? #(and (<= % js/Math.PI) (>= % (- js/Math.PI))) [a b c d])]}
  (fn [[x y z]]
    [(- (js/Math.sin (* a y)) (js/Math.cos (* b x)))
     (- (js/Math.sin (* c x)) (js/Math.cos (* d y)))
     (- (js/Math.sin (* 2.0 x)) (js/Math.cos (* 2.0 y)))]))

(defn random-points [minimum maximum]
  (let [random-val #(+ (rand (- maximum minimum)) minimum)]
    (map vec (partition 3 (repeatedly random-val)))))

(defn update-points [owner]
  (let [{:keys [ifs points]} (om/get-state owner)
        new-points           (vec (map ifs points))]
    (om/update-state! owner (fn [prev] (merge prev { :points new-points })))))

(defn state-from-params [ifs-params]
  { :ifs    (apply de-jong-ifs ifs-params)
    :points (take points-to-draw (random-points -2.0 2.0)) })

(defn points-calculator [ifs-params owner]
  (reify
    om/IInitState
    (init-state [_]
      (state-from-params ifs-params))
    om/IDidMount
    (did-mount [_]
      (update-points owner))
    om/IDidUpdate
    (did-update [_ _ _]
      (update-points owner))
    om/IWillReceiveProps
    (will-receive-props [_ _]
      (om/update-state! owner (fn [_] (state-from-params ifs-params))))
    om/IRenderState
    (render-state [_ state]
      (om/build ifs-viewer (:points state)))))
