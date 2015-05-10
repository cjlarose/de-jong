(ns de-jong.points-calculator
  (:require [om.core :as om]
            [om.dom :as dom]
            [de-jong.ifs-viewer :refer [ifs-viewer]]))

;; (def points-to-draw (js/Math.pow 2 14))

(def worker-script
  "function deJong(a, b, c, d) {
     return function(x, y, z) {
       return [Math.sin(a * y) - Math.cos(b * x),
               Math.sin(c * x) - Math.cos(d * y),
               Math.sin(2.0 * x) - Math.cos(2.0 * y)];
     }
   }

   function randomVal(min, max) {
     return Math.random() * (max - min) + min;
   }

   function randomPoints(min, max, len) {
      var arr = new Array(len);
      for (var i = 0; i < len; i++)
        arr[i] = [randomVal(min, max),
                  randomVal(min, max),
                  randomVal(min, max)];
      return arr;
   }

   self.onmessage = function(e) {
     var fn = deJong.apply(null, e.data);
     var numPoints = Math.pow(2, 14);
     var points = randomPoints(-2.0, 2.0, numPoints);

     //postMessage([[-1, -1, -1]]);
     //postMessage(points);

     (function loop() {
       //postMessage('Worker: ' + fn(0, 0, 0));
       setTimeout(loop, 17);
       postMessage(points);

       for (var i = 0; i < numPoints; i++)
         points[i] = fn.apply(null, points[i]);

     })();
   }")

;; (defn de-jong-ifs [a b c d]
;;   {:pre [(every? #(and (<= % js/Math.PI) (>= % (- js/Math.PI))) [a b c d])]}
;;   (fn [[x y z]]
;;     [(- (js/Math.sin (* a y)) (js/Math.cos (* b x)))
;;      (- (js/Math.sin (* c x)) (js/Math.cos (* d y)))
;;      (- (js/Math.sin (* 2.0 x)) (js/Math.cos (* 2.0 y)))]))
;; 
;; (defn random-points [minimum maximum]
;;   (let [random-val #(+ (rand (- maximum minimum)) minimum)]
;;     (map vec (partition 3 (repeatedly random-val)))))

;; (defn update-points [owner]
;;   (let [{:keys [ifs points]} (om/get-state owner)
;;         new-points           (vec (map ifs points))]
;;     (om/update-state! owner (fn [prev] (merge prev { :points new-points })))))

;; (defn state-from-params [ifs-params]
;;   { :ifs    (apply de-jong-ifs ifs-params)
;;     :points (take points-to-draw (random-points -2.0 2.0)) })

(defn points-calculator [ifs-params owner]
  (reify
    om/IInitState
    (init-state [_]
      {:points []})
    om/IDidMount
    (did-mount [_]
      (let [blob   (js/Blob. #js [worker-script], #js { :type "application/javascript" })
            worker (js/Worker. (.createObjectURL js/URL blob))]
            ;;(set! (.-onmessage worker) (fn [e] (println (.-data e))))
            (set! (.-onmessage worker) (fn [e] (om/set-state! owner :points (.-data e))))
            (.postMessage worker #js [0.97 -1.9 1.38 -1.5])))
    ;; om/IDidUpdate
    ;; (did-update [_ _ _]
    ;;   (update-points owner))
    ;; om/IWillReceiveProps
    ;; (will-receive-props [_ _]
    ;;   (om/update-state! owner (fn [_] (state-from-params ifs-params))))
    om/IRenderState
    (render-state [_ state]
      ;;(println (take 5 (:points state)))
      (om/build ifs-viewer (:points state)))))
