(ns mcms.core
  (:require [fleetdb.client :as fleetdb])
  (:use [mcms covers media collection users entrez]
	[compojure]))

(defonce *db* (atom nil))

(defonce *app* (atom nil))

(defroutes mcms-routes
  (GET "/covers/:isbn"
       (serve-file "covers" (:isbn params)))
  (POST "/covers/:isbn"
	(add-cover (:isbn params) (get-in params [:cover :tempfile])))
  (GET "/media/:isbn"
       (show-item (first (get-items @*db* [(:isbn params)]))))
  (GET "/media"
       (list-media @*db*))
  (POST "/media"
	(add-item @*db* params)
    (list-media @*db*))
  (GET "/users"
       (show-users @*db*))
  (POST "/users"
	(add-user @*db* (:username params))
	(show-users @*db*))
  (POST "/:username" (add-to-collection @*db* params))
  (GET "/:username"
       (list-collection @*db* (:username params)))
  (GET "*"
        (or (serve-file "public" (:* params)) :next))
  (ANY "*"
       [404 "Page Not Found"]))

(decorate mcms-routes (with-multipart))


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


