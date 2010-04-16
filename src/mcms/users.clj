(ns mcms.users
  (:require [fleetdb.client :as fleetdb])
  (:use [mcms.collection]
	[compojure]
	[clojure.contrib.duck-streams :only [copy]])
  (:import [java.io File]))

(defn get-user-id 
  ([username]
     ["select" "users" {"where" ["=" :name username]}])
  ([db username]
     (get (first (db ["select" "users" {"where" ["=" :name username]}])) "id")))