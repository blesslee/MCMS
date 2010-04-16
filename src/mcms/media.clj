(ns mcms.media
  (:require [fleetdb.client :as fleetdb])
  (:use [mcms covers] 
	[compojure]
	[clojure.contrib.duck-streams :only [copy]])
  (:import [java.io File]))

(defn count-item 
  ([isbn]
     ["count" "media" {"where" ["=" :id isbn]}])
  ([db isbn]
     (db (count-item isbn))))

(defn add-item [db {:keys [isbn author title cover] :as item}]
  (let [isbn (Integer/parseInt isbn)]
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