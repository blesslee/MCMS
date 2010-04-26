(ns mcms.collection
  (:use [net.cgrand.enlive-html]
	[mcms db media users]))

(deftemplate collection-template "mcms/collection-template.html" [current username collection]
  [:.current] (do->
	    (content (str current))
	    (set-attr :href (str current)))
  [:.collection] (content (str username "'s Collection"))
  [:#add-media] (do-> (after (add-media-form (str "/" username))))
  [:#item] (content (map item collection)))


(defn user-collection [db username]   
  (let [uid (get-user-id db username)
	isbns (map #(get % "isbn") (owned db uid))]
    (get-media db isbns)))

(defn add-to-collection 
  ([db {username :username, isbn :isbn :as item}]
     (let [uid (get-user-id db username)]
       (when (zero? (count-item db isbn)) (add-item db item))
       (db ["insert" "collection" {:id (next-id db "collection") :isbn isbn, :owner uid}]))))

(defn show-collection [current username media] (apply str (collection-template current username media)))

(defn show-item [media] (apply str (emit* (item media))))

(defn show-user-collection [db current username]
  (show-collection current username (user-collection db username)))
