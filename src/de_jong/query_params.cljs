(ns de-jong.query-params
  (:require [clojure.string :refer [split join]]
            [cljs.core.async :refer [chan put!]]))

(defn query-params [query-string]
  (map #(split % #"=") (split (subs query-string 1) #"&")))

(defn- encode-app-state [app-state]
  (->> app-state
       (clj->js)
       (.stringify js/JSON)
       (js/btoa)))

(defn- decode-app-state [b64]
  (js->clj (.parse js/JSON (js/atob b64)) :keywordize-keys true))

(defn extract-app-state [query-string]
  (->> query-string
       (query-params)
       (filter (fn [[k v]] (and (= k "s") v)))
       (first)
       (second)
       (decode-app-state)))

(defn- state-url [app-state]
  (let [l (.-location js/window)
        encoded-state (encode-app-state app-state)
        query-string (str "?s=" encoded-state)]
    (str (.-protocol l) "//" (.-host l) (.-pathname l) query-string)))

(defn update-history!
  ([app-state]
     (update-history! app-state false))
   ([app-state replace]
      (let [url (state-url app-state)
            f   (aget js/history (if replace "replaceState" "pushState"))]
        (.call f js/history (clj->js app-state) "" url))))

(defn popstate-changes []
  (let [comm (chan)]
    (.addEventListener
      js/window
      "popstate"
      (fn [e]
        (let [state (js->clj (.-state e) :keywordize-keys true)
              msg   { :state state }]
          (put! comm msg))))
    comm))
