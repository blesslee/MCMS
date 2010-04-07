(ns mcms.collection
  (:require [net.cgrand.enlive-html :as html]))

(def *item-selector* (html/selector [:.item]))

(html/defsnippet item "mcms/main-template.html" *item-selector*
  [media] 
  [:td.title] (html/content (get media "title"))
  [:td.author] (html/content (get media "author")))

(html/deftemplate collection-template "mcms/main-template.html"
  [username media]
  [:title#title] (html/content (str username "'s Collection"))
  [:.item] (html/content (map item media)))

(defn collection [username media] (apply str (collection-template username media)))