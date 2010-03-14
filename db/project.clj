(defproject MCMS "0.1.0-SNAPSHOT"
 :description "A media collection manager."
 :main fleetdb.server
 :dependencies [[fleetdb "0.1.1-SNAPSHOT"]]
 :dev-dependencies [[leiningen-run "0.2"]])

(ns leiningen.run-db
  (:require [fleetdb.server :as fleetdb]))


(defn run-db [project & args]
  (fleetdb/-main "-f" "db.fdb"))
