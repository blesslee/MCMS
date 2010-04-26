(ns mcms.core
  (:require [fleetdb.client :as fleetdb])
  (:use [mcms covers media collection users entrez camera login]
	[compojure]))

(defonce *db* (atom nil))
(defonce *app* (atom nil))

(defroutes mcms-routes
  (GET "/"
        (show-tolog session @*db*))
  (POST "/login-face"
        (face-login @*db*))
  (POST "/login-passwd"
        (passwd-login @*db* (:username params) (:password params)))
  (GET "/logout"
        (logout-user session))
  (GET "/covers/:isbn"
       (serve-file "covers" (:isbn params)))
  (POST "/covers/:isbn"
	(add-cover (:isbn params) (get-in params [:cover :tempfile])))
  (GET "/media/:isbn"
       (show-item (first (get-media @*db* [(:isbn params)]))))
  (GET "/media"
       (show-media (:current-user session) (get-media @*db*)))
  (POST "/media"
	(add-item @*db* params)
	(show-media (:current-user session) (get-media @*db*)))
  (POST "/search"
	(let [search-results (search-cover (get-in params [:cover :tempfile]))]
	  (show-media (:current-user session) (get-media @*db* (keys search-results)) (vals search-results))))
  (GET "/users"
       (show-users @*db*))
  (POST "/users"
	(add-user-passwd @*db* (:username params) (:password params))
	(show-users @*db*))
  (POST "/:username" 
	(add-to-collection @*db* (assoc params :username (:current-user session)))
	(redirect-to (str "/" (:current-user session))))
  (GET "/:username"
       (show-user-collection @*db* (:current-user session) (:username params)))
  (GET "*"
       (or (serve-file "public" (:* params)) :next))
  (ANY "*"
       [404 "Page Not Found"]))

(decorate mcms-routes (with-multipart))
(decorate mcms-routes (with-session :memory))

;; ========================================
;; The App
;; ========================================


(defn start-app []
  (if @*app* (stop @*app*))
  (compute-cover-histograms)
  (reset! *db* (fleetdb/connect {:host "127.0.0.1", :port 3400}))
  (reset! *app* (run-server {:port 8080}
                            "/*" (servlet mcms-routes))))
(defn stop-app []
  (when @*app* (stop @*app*)))


