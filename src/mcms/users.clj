(ns mcms.users
  (:require [fleetdb.client :as fleetdb])
  (:use [mcms collection db]
	[compojure]
	[clojure.contrib.duck-streams :only [copy]]
	[net.cgrand.enlive-html])
  (:import [java.io File]))

(defsnippet user-template "mcms/users-template.html" (selector [:#item])
  [{:strs [name]}]
  [:.user]   (do-> 
	      (set-attr :href (str "/" name)) 
	      (content name)))

(defsnippet user-form "mcms/addUser.html" (selector [:form])
  [destination]
  [:form] (set-attr :action destination))

(deftemplate users-template "mcms/users-template.html" [users]
  [:#item] (content (map user-template users))
  [:#add-user] (do-> (after (user-form "/users")))
  )

(defn get-user-id 
  ([username]
     ["select" "users" {"where" ["=" :name username]}])
  ([db username]
     (get (first (db ["select" "users" {"where" ["=" :name username]}])) "id")))

(defn users 
  ([]
     ["select" "users"])
  ([db]
     (db (users))))

;(defn add-user
;  ([db username]
;     (db ["insert" "users" {:id (next-id db "users"), :name username}])))

(defn add-user-passwd
  ([db username password]
     (db ["insert" "users" {:id (next-id db "users"), :name username :passwd password}])))
     
(defn show-users [db]
  (apply str (users-template (users db))))