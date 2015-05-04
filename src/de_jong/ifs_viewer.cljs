(ns de-jong.ifs-viewer
  (:require [om.core :as om]
            [om.dom :as dom]))

(def fill-color [0 192 0 255])
(def points-per-frame 1e4)
(def points-to-draw 1e5)

(defn de-jong-ifs [a b c d]
  {:pre [(every? #(and (<= % js/Math.PI) (>= % (- js/Math.PI))) [a b c d])]}
  (fn [[x y]]
    [(- (js/Math.sin (* a y)) (js/Math.cos (* b x)))
     (- (js/Math.sin (* c x)) (js/Math.cos (* d y)))]))

(defn setup-canvas [owner]
  (let [canvas (om/get-node owner "canvas")
        context (.getContext canvas "2d")]
    (set! (.-fillStyle context) "rgba(0, 192, 0, 0.1)")
    (.scale context 200 200)
    (.translate context 2 2)))

(defn render-in-canvas [owner [w h] points]
  (let [canvas  (om/get-node owner "canvas")
        context (.getContext canvas "2d")]
    (.clearRect context -2 -2 4 4)
    (doseq [[x y] points]
      (.fillRect context x y 1e-2 1e-2))))

(defn compute-next-points [ifs initial-point num-points]
  (let [new-points (vec (take num-points (iterate ifs initial-point)))]
    {:new-points new-points
     :next-point (ifs (last new-points))}))

(defn start-timer [owner]
  (let [tick (fn self []
               (if (>= (count (om/get-state owner :points)) points-to-draw)
                 (om/set-state! owner :next-point nil)
                 (let [{:keys [a b c d]} (:ifs-params (om/get-props owner))
                       ifs               (de-jong-ifs a b c d)
                       initial-point     (:next-point (om/get-state owner))
                       {:keys [new-points next-point]} (compute-next-points ifs initial-point points-per-frame)]
                   (om/update-state! owner (fn [{:keys [points]}]
                                             {:points (vec (concat points new-points))
                                              :next-point next-point}))
                   (.requestAnimationFrame js/window self))))]
    (.requestAnimationFrame js/window tick)))

(defn ifs-viewer [{:keys [ifs-params point-data] :as data} owner]
  (let [w 800 h 800]
    (reify
      om/IInitState
      (init-state [_]
        {:points [] :next-point [0 0]})
      om/IDidMount
      (did-mount [this]
        (setup-canvas owner)
        (start-timer owner))
      om/IDidUpdate
      (did-update [this prev-props prev-state]
        (let [points (om/get-state owner :points)]
          (render-in-canvas owner [w h] points)))
      om/IWillReceiveProps
      (will-receive-props [this next-props]
        (om/update-state! owner (fn [_] {:points [] :next-point [0 0]}))
        (start-timer owner))
      om/IRender
      (render [this]
        (dom/div #js {:id "ifs-viewer"}
          (dom/canvas #js {:ref "canvas" :width w :height h}))))))
