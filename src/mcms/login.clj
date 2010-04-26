(ns mcms.login
  (:require [fleetdb.client :as fleetdb] [clojure.xml :as xml])
  (:use [mcms media camera] 
	[compojure]
	[clojure.contrib.duck-streams :only [copy]]
	[net.cgrand.enlive-html])
  (:import [java.io File]
           [javax.swing SwingUtilities]))
  
(defonce *current-user* (atom nil))

(defsnippet passwd-login-form "mcms/passwd-login.html" (selector [:form])
  [destination]
  [:form] (set-attr :action destination))

(defsnippet face-detect-form "mcms/face-detect.html" (selector [:form])
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
  (reset! *current-user* username)
  (list-media db))

(defn show-tolog [db]
  (if (= @*current-user* nil)
  (login-template)
  (show-loggedin db @*current-user*))) 

(defn logout-user []
  (reset! *current-user* nil)
  (login-template))
  
(defn get-detect-result [db]
  (let [login-promise (promise)]
    (SwingUtilities/invokeAndWait #(face-detect db login-promise))
    (show-loggedin db @login-promise)))
  
(defn face-login [db]
  (get-detect-result db)
  #_(if (= (get-detect-result db) 1)))
  
(defn passwd-login [db username password]
  (if (check-passwd db username password)
    (show-loggedin db username)
    (show-tolog db)))