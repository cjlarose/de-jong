(ns de-jong.components.fa-icon
  (:require [om.dom :as dom]))

(defn fa-icon [icon]
  (dom/i #js { :className (str "fa fa-" icon) } ))
