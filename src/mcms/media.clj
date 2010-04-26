(ns mcms.media
  (:require [fleetdb.client :as fleetdb] [clojure.xml :as xml])
  (:use [mcms covers entrez] 
	[compojure]
	[clojure.contrib.duck-streams :only [copy]]
	[net.cgrand.enlive-html])
  (:import [java.io File]))

(defsnippet add-media-form "mcms/addMedia.html" [:form]
  [destination]
  [:form] (set-attr :action destination))

(defsnippet search-media-form "mcms/searchMedia.html" [:form]
  [destination]
  [:form] (set-attr :action destination))

(defsnippet item "mcms/media-template.html" [:#item] [{:strs [id title author]}] 
  [:.isbn] (do->
	    (content (str id))
	    (set-attr :href (str "/media/" id)))
  [:.title] (content title)
  [:.author] (content author)
  [:.cover] (set-attr :src (str "/covers/" id))
  [:.rank]  nil)

(defsnippet ranked-item "mcms/media-template.html" [:#item] [{:strs [id title author]} rank]
  [:.isbn] (do->
	    (content (str id))
	    (set-attr :href (str "/media/" id)))
  [:.title] (content title)
  [:.author] (content author)
  [:.cover] (set-attr :src (str "/covers/" id))
  [:.rank] (content (str rank)))

(deftemplate media-template "mcms/media-template.html" [current collection]
  [:.current] (do->
	    (content (str current))
	    (set-attr :href (str current)))
  [:#add-media] (do-> (after (add-media-form "/media")))
  [:#search-media] (do-> (after (search-media-form "/search")))
  [:#item] (content (map item collection)))

(deftemplate ranked-media-template "mcms/media-template.html" [current collection rank]
  [:.current] (do->
	    (content (str current))
	    (set-attr :href (str current)))
  [:#add-media] (do-> (after (add-media-form "/media")))
  [:#search-media] (do-> (after (search-media-form "/search")))
  [:#item] (content (map ranked-item collection rank)))

(defn count-item 
  ([isbn]
     ["count" "media" {"where" ["=" :id isbn]}])
  ([db isbn]
     (db (count-item isbn))))

(defn add-item [db {:keys [isbn author title cover] :as item}]
  (let [author (get-author isbn)
        title (get-title isbn)
        cover-source (get-cover isbn cover)]
    ; TODO: Check to make sure isbn isn't null
    ; TODO: Make sure ISBN is valid
    ; TODO: Query libraryThing for any missing data, including coverart
    (db ["checked-write"
	 (count-item isbn) 0 ; Don't insert the book if its ISBN already exists
	 ["insert" "media" {:id isbn :author author :title title}]])
    (add-cover isbn cover-source)))

(defn- get-items
  ([isbns]
     ["select" "media" {"where" ["in" :id (vec isbns)]}])
  ([db isbns]
     (db (get-items isbns))))

(defn owned
  ([uid]
     ["select" "collection" {"where" ["=" :owner uid]}])
  ([db uid]
     (db (owned uid))))

(defn get-media 
  ([]
     ["select" "media"])
  ([db]
     (db (get-media)))
  ([db isbns]
     (db (get-items isbns))))

(defn show-media 
  ([current media]
     (media-template current media))
  ([current media rank]
     (ranked-media-template current media rank)))
