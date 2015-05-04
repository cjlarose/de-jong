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

(defn get-index [[w h] [x y]]
  (* 4 (+ (* w (.floor js/Math y)) (.floor js/Math x))))

(defn get-color [{size :size data :data} [x y]]
  (let [idx (get-index size [x y])]
    [(aget data idx)
     (aget data (+ 1 idx))
     (aget data (+ 2 idx))
     (aget data (+ 3 idx))]))

(defn set-color [{size :size data :data} [x y] [r g b a]]
  (let [idx (get-index size [x y])]
    (aset data idx r)
    (aset data (+ 1 idx) g)
    (aset data (+ 2 idx) b)
    (aset data (+ 3 idx) a)))

(defn alpha-blend-colors [[rx gx bx _] [ry gy by ay]]
  (let [fg-alpha (/ ay 255)]
    [(+ (* ry fg-alpha) (* rx (- 1 fg-alpha)))
     (+ (* gy fg-alpha) (* gx (- 1 fg-alpha)))
     (+ (* by fg-alpha) (* bx (- 1 fg-alpha)))
     255]))

(defn draw-points [[w h] ctx points]
  (let [image-data     (.createImageData ctx w h)
        data-with-size {:data (.-data image-data) :size [w h]}
        add-color      (fn [pos color]
                         (set-color
                           data-with-size
                           pos
                           color))]
                           ;;(alpha-blend-colors (get-color data-with-size pos) color)))]
    (doseq [[x y] points]
      (add-color [(* (+ x 2) 200) (* (+ y 2) 200)] fill-color))
    image-data))

(defn render-in-canvas [owner [w h] points]
  (let [canvas  (om/get-node owner "canvas")
        context (.getContext canvas "2d")
        image-data (draw-points [w h] context points)]
    (.putImageData context image-data 0 0)))

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
                   (println "tick")
                   (println initial-point)
                   (om/update-state! owner (fn [{:keys [points]}]
                                             {:points (vec (concat points new-points))
                                              :next-point next-point}))
                   (println "calling self")
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
        (println "did-mount")
        (start-timer owner))
      om/IDidUpdate
      (did-update [this prev-props prev-state]
        (let [points (om/get-state owner :points)]
          (println "did-update")
          (println "points to draw: " (count points))
          (render-in-canvas owner [w h] points)))
      om/IWillReceiveProps
      (will-receive-props [this next-props]
        (println "receiving props")
        (om/update-state! owner (fn [_] {:points [] :next-point [0 0]}))
        (start-timer owner))
      om/IRender
      (render [this]
        (dom/div #js {:id "ifs-viewer"}
          (dom/canvas #js {:ref "canvas" :width w :height h}))))))
