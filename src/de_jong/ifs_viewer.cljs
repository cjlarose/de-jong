(ns de-jong.ifs-viewer
  (:require [om.core :as om]
            [om.dom :as dom]))

(def points-to-draw 1e4)

(defn de-jong-ifs [a b c d]
  {:pre [(every? #(and (<= % js/Math.PI) (>= % (- js/Math.PI))) [a b c d])]}
  (fn [[x y]]
    [(- (js/Math.sin (* a y)) (js/Math.cos (* b x)))
     (- (js/Math.sin (* c x)) (js/Math.cos (* d y)))]))

(defn setup-canvas [owner]
  (let [canvas (om/get-node owner "canvas")
        context (.getContext canvas "2d")]
    (set! (.-fillStyle context) "rgba(0, 192, 0, 0.5)")
    (.scale context 200 200)
    (.translate context 2 2)))

(defn render-in-canvas [owner [w h] points]
  (let [canvas  (om/get-node owner "canvas")
        context (.getContext canvas "2d")]
    (.clearRect context -2 -2 4 4)
    (doseq [[x y] points]
      (.fillRect context x y 1e-2 1e-2))))

(defn start-timer [owner]
  (let [tick (fn self []
               (let [ifs               (om/get-state owner :ifs)
                     old-points        (om/get-state owner :points)
                     new-points        (map ifs old-points)]
                 (om/set-state! owner :points new-points)
                 (.requestAnimationFrame js/window self)))]
    (.requestAnimationFrame js/window tick)))

(defn random-points [minimum maximum]
  (let [difference (- maximum minimum)
        random-val #(+ (* (js/Math.random) difference) minimum)]
    (map vec (partition 2 (repeatedly random-val)))))

(defn ifs-viewer [{:keys [a b c d] :as ifs-params} owner]
  (let [w 800 h 800]
    (reify
      om/IInitState
      (init-state [_]
        {:points (take points-to-draw (random-points (- js/Math.PI) js/Math.PI))
         :ifs (de-jong-ifs a b c d)})
      om/IDidMount
      (did-mount [_]
        (setup-canvas owner)
        (start-timer owner))
      om/IDidUpdate
      (did-update [_ _ _]
        (let [points (om/get-state owner :points)]
          (render-in-canvas owner [w h] points)))
      om/IWillReceiveProps
      (will-receive-props [_ _]
        (let [ifs (de-jong-ifs a b c d)
              points (take points-to-draw (random-points (- js/Math.PI) js/Math.PI))]
          (om/update-state! owner (fn [_] {:points points :ifs ifs}))))
      om/IRender
      (render [_]
        (dom/div #js {:id "ifs-viewer"}
          (dom/canvas #js {:ref "canvas" :width w :height h}))))))
