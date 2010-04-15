(ns mcms.core
  (:require [fleetdb.client :as fleetdb])
  (:use [mcms.collection]
	[compojure]
	[clojure.contrib.duck-streams :only [copy]])
  (:import [java.io File]))

(defonce *db* (atom nil))

(defonce *app* (atom nil))

(defn get-user-id [username]
  (get (first (@*db* ["select" "users" {"where" ["=" :name username]}])) "id"))

(defn next-id [table]
  (inc (@*db* ["count" table])))

(defn count-item 
  ([isbn]
     ["count" "media" {"where" ["=" :id isbn]}])
  ([db isbn]
     (db (count-item isbn))))

(defn add-item [{:keys [isbn author title cover] :as item}]
  (let [db @*db*, 
	isbn (Integer/parseInt isbn)]
    (db ["checked-write"
	 (count-item isbn) 0 ; Don't insert the book if its ISBN already exists
	 ["insert" "media" {:id isbn :author author :title title}]])
    (add-cover isbn cover)))

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

(defn user-collection [username]   
  (let [db @*db*
	uid (get-user-id username)
	isbns (map #(get % "isbn") (owned db uid))]
    (get-items db isbns)))

(defn collection-list [username]
  (show-collection username (user-collection username)))

(defn cover
  ([isbn]
     ["select" "covers" {"where" ["=" :id isbn]}])
  ([db isbn]
     (db (cover isbn))))

(defn show-cover [isbn]
  (let [isbn (if (integer? isbn) isbn (Integer/parseInt isbn))]
    (get (first (cover @*db* isbn)) "cover")))

(defn add-cover 
  ([isbn cover]
     (let [src (:tempfile cover)
	   dest (File. (str "./covers/" isbn))]
       (when (.exists src)
	 (when-not (.exists dest) (.createNewFile dest))
	 (copy src dest))))
  ([{:keys [isbn cover]}]
     (add-cover isbn cover)))
  

(defn add-to-collection [{username :username, isbn :isbn :as item}]
  (let [uid (get-user-id username)
	isbn (if (integer? isbn) isbn (Integer/parseInt isbn))]
    (when (zero? (count-item @*db* isbn)) (add-item item))
    (@*db* ["insert" "collection" {:id (next-id "collection") :isbn isbn, :owner uid}])))

(defroutes mcms-routes
  (GET "/covers/:isbn"
       (serve-file "covers" (:isbn params)))
  (POST "/covers/:isbn"
	(add-cover params))
					;(GET "/media" (show-search-page))
  (GET "/media/:isbn"
       (show-item (first (get-items @*db* [(Integer/parseInt (:isbn params))]))))
  (POST "/media"
	(add-item params))
					;(POST "/:username" (add-user params))
  (POST "/:username/:isbn" (add-to-collection params))
  (GET "/:username"
       (collection-list (:username params)))
  (ANY "*"
       [404 "Page Not Found"]))

(decorate mcms-routes (with-multipart))


;; ========================================
;; The App
;; ========================================



(defn start-app []
  (if @*app* (stop @*app*))
  (reset! *db* (fleetdb/connect {:host "127.0.0.1", :port 3400}))
  (reset! *app* (run-server {:port 8080}
                            "/*" (servlet mcms-routes))))
(defn stop-app []
  (when @*app* (stop @*app*)))
