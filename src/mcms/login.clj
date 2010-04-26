(ns mcms.login
  (:require [fleetdb.client :as fleetdb] [clojure.xml :as xml])
  (:use [mcms media camera collection] 
	[compojure]
	[clojure.contrib.duck-streams :only [copy]]
	[net.cgrand.enlive-html])
  (:import [java.io File]
           [javax.swing SwingUtilities]))
  
;(defonce *current-user* (atom nil))

(defsnippet passwd-login-form "mcms/passwd-login.html" [:form]
  [destination]
  [:form] (set-attr :action destination))

(defsnippet face-detect-form "mcms/face-detect.html" [:form]
  [destination]
  [:form] (set-attr :action destination))

(deftemplate login-template "mcms/login-template.html" []
  [:#passwd-logging] (do-> (after (passwd-login-form "/login-passwd")))
  [:#face-detect] (do-> (after (face-detect-form "/login-face")))  
  ;[:#item] (content (map item collection))
  )

(defn query-password 
  ([username]
        ["select" "users" {"where" ["=" :name username]}])
  ([db username]
        (get (first (db (query-password username))) "passwd")))

(defn check-passwd [db username password]
  (= password (query-password db username)))
  
(defn show-loggedin [db {:keys [username selected]}]
  ;(println "Logged in" username) interferes!
  #_(reset! *current-user* username)
  ; Return a vector containing new session and the html
  [(session-assoc :current-user username)
  (redirect-to (str "/" username))#_(show-user-collection db username)])

;(defn show-tolog [db]
;  (if #_(= @*current-user* nil) (= current-user nil)
;  (login-template)
;  #_(show-loggedin db @*current-user*) (show-loggedin db current-user))) 

(defn show-tolog [session db]
  (if (contains? session :current-user)
    (show-loggedin db (:current-user session))
    (login-template)))

(defn logout-user [session]
  #_(reset! *current-user* nil)
  (if (contains? session :current-user)
    [(session-dissoc :current-user)
    (login-template)]))
  
(defn get-detect-result [db]
  (let [login-promise (promise)]
    (SwingUtilities/invokeAndWait #(face-detect db login-promise))
    (show-loggedin db @login-promise)))
  
(defn face-login [db]
  (get-detect-result db)
  #_(if (= (get-detect-result db) 1)))
  
(defn passwd-login [db username password]
  (if (check-passwd db username password)
    (show-loggedin db {:username username})
    (show-tolog db)))