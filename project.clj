(defproject MCMS "0.1.0-SNAPSHOT"
 :description "A media collection manager."
 :dependencies [[fleetdb "0.1.1-SNAPSHOT"]
                [enlive "1.0.0-SNAPSHOT"]
                [compojure "0.3.2"]
                [fleetdb-client "0.1.1-SNAPSHOT"]
		[org.clojars.remleduff/javacv "20100416"]
		[net.java.dev.jna/jna "3.2.4"]]
 :dev-dependencies [[leiningen/lein-swank "1.2.0-SNAPSHOT"]]
 :namespaces [mcms.core]
 :main-class mcms.core
 :repositories {"dev.java.net" "http://download.java.net/maven/2/"})

