(ns mcms.core
  (:require [fleetdb.client :as fleetdb])
  (:use [mcms.collection]
	[compojure]))

(defonce *db* (atom nil))

(defonce *app* (atom nil))

(defn user-collection
  [username]   (@*db* ["select" "collection" {"where" ["=" :owner (str username)]}]))

(defroutes mcms-routes
  (GET "/"
       (collection "remleduff" (user-collection "remleduff")))
  (ANY "*"
       [404 "Page Not Found"]))

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
