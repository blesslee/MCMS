(defproject MCMS "0.1.0-SNAPSHOT"
 :description "A media collection manager."
 :dependencies [[fleetdb "0.1.1-SNAPSHOT"]]
 :dev-dependencies [[fleetdb-client "0.1.1-SNAPSHOT"]
		    [org.clojars.gilbertl/vimclojure "2.1.2"]
		    [swank-clojure "1.1.0"]]
 :namespaces [mcms.core]
 :main-class mcms.core)