(ns mcms.media
  (:require [fleetdb.client :as fleetdb])
  (:use [mcms covers] 
	[compojure]
	[clojure.contrib.duck-streams :only [copy]]
	[net.cgrand.enlive-html])
  (:import [java.io File]))

(defsnippet add-media-form "mcms/addMedia.html" (selector [:form])
  [destination]
  [:form] (set-attr :action destination))

(defsnippet search-media-form "mcms/searchMedia.html" (selector [:form])
  [destination]
  [:form] (set-attr :action destination))

(defsnippet item "mcms/media-template.html" (selector [:#item])
  [{:strs [id title author]}] 
  [:.isbn] (do->
	    (content (str id))
	    (set-attr :href (str "/media/" id)))
  [:.title] (content title)
  [:.author] (content author)
  [:.cover] (set-attr :src (str "/covers/" id)))

(deftemplate media-template "mcms/media-template.html" [collection]
  [:#add-media] (do-> (after (add-media-form "/media")))
  [:#search-media] (do-> (after (search-media-form "/media")))
  [:#item] (content (map item collection)))

(defn count-item 
  ([isbn]
     ["count" "media" {"where" ["=" :id isbn]}])
  ([db isbn]
     (db (count-item isbn))))

(defn add-item [db {:keys [isbn author title cover] :as item}]
  (let [isbn (Integer/parseInt isbn)]
    ; TODO: Check to make sure isbn isn't null
    ; TODO: Make sure ISBN is valid
    ; TODO: Query libraryThing for any missing data, including coverart
    (db ["checked-write"
	 (count-item isbn) 0 ; Don't insert the book if its ISBN already exists
	 ["insert" "media" {:id isbn :author author :title title}]])
    (when (.exists (:tempfile cover)) (add-cover isbn (:tempfile cover)))))

(defn get-items
  ([isbns]
     ["select" "media" {"where" ["in" :id isbns]}])
  ([db isbns]
     (db (get-items isbns))))

(defn owned
  ([uid]
     ["select" "collection" {"where" ["=" :owner uid]}])
  ([db uid]
     (db (owned uid))))

(defn list-media [db] 
  (media-template (db ["select" "media"])))