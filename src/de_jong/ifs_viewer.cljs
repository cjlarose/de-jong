(ns de-jong.ifs-viewer
  (:require [om.core :as om]
            [om.dom :as dom]
            [cljsjs.three]))

(def points-to-draw 1e4)

(defn de-jong-ifs [a b c d]
  {:pre [(every? #(and (<= % js/Math.PI) (>= % (- js/Math.PI))) [a b c d])]}
  (fn [[x y z]]
    [(- (js/Math.sin (* a y)) (js/Math.cos (* b x)))
     (- (js/Math.sin (* c x)) (js/Math.cos (* d y)))
     (- (js/Math.sin (* 2.0 x)) (js/Math.cos (* 2.0 y)))]))

(defn points-to-vertices [points]
  (apply array (map (fn [[x y z]] (js/THREE.Vector3. x y z)) points)))

(defn get-new-points [owner]
  (let [ifs        (om/get-state owner :ifs)
        old-points (om/get-state owner :points)
        new-points (vec (map ifs old-points))]
    new-points))

(defn random-points [minimum maximum]
  (let [difference (- maximum minimum)
        random-val #(+ (* (js/Math.random) difference) minimum)]
    (map vec (partition 3 (repeatedly random-val)))))

(defn ifs-viewer [{:keys [a b c d] :as ifs-params} owner]
  (let [w 800 h 800]
    (reify
      om/IInitState
      (init-state [_]
        (let [geometry (js/THREE.Geometry.)
              scene    (js/THREE.Scene.)
              camera   (js/THREE.PerspectiveCamera. 45 (/ w h) 0.1 1000)
              material (js/THREE.PointCloudMaterial. #js { :size 0.02 :color 0x00cc00 })
              cloud    (js/THREE.PointCloud. geometry material)]
          (.add scene cloud)
          (set! (.-z (.-position camera)) 8)
          { :points (take points-to-draw (random-points (- js/Math.PI) js/Math.PI))
            :ifs (de-jong-ifs a b c d)
            :geometry geometry
            :scene scene
            :camera camera
            :cloud cloud }))
      om/IDidMount
      (did-mount [_]
        (let [renderer (js/THREE.WebGLRenderer. #js { :canvas (om/get-node owner "canvas")
                                                      :alpha true })]
          (om/update-state! owner (fn [prev] (merge prev {:points (get-new-points owner)
                                                          :renderer renderer})))))
      om/IDidUpdate
      (did-update [_ _ _]
        (let [{:keys [geometry points renderer scene camera cloud]} (om/get-state owner)]
          (set! (.-vertices geometry) (points-to-vertices points))
          (set! (.-verticesNeedUpdate geometry) true)
          (set! (.-y (.-rotation cloud)) (+ 0.01 (.-y (.-rotation cloud))))
          (set! (.-z (.-rotation cloud)) (+ 0.01 (.-z (.-rotation cloud))))
          (.render renderer scene camera)
          (om/update-state! owner (fn [prev] (merge prev {:points (get-new-points owner)})))))
      om/IWillReceiveProps
      (will-receive-props [_ _]
        (let [ifs (de-jong-ifs a b c d)
              points (take points-to-draw (random-points (- js/Math.PI) js/Math.PI))]
          (om/update-state! owner (fn [prev] (merge prev {:points points :ifs ifs})))))
      om/IRender
      (render [_]
        (dom/div #js {:id "ifs-viewer"}
          (dom/canvas #js {:ref "canvas" :width w :height h}))))))
