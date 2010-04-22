(defproject MCMS "0.1.0-SNAPSHOT"
 :description "A media collection manager."
 :dependencies [[fleetdb "0.1.1-SNAPSHOT"]
                [enlive "1.0.0-SNAPSHOT"]
                [compojure "0.3.2"]
                [org.clojars.sergey-miryanov/clj-native "0.8.2.x"]
                [fleetdb-client "0.1.1-SNAPSHOT"]]
 :namespaces [mcms.core]
 :main-class mcms.core)
