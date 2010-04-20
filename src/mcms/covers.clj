(ns mcms.covers
  (:import [java.io File]
           [java.net URL])
  (:use [clojure.contrib.duck-streams :only [copy]]))

(defn add-cover 
  ([isbn cover]
     (let [dest (File. (str "./covers/" isbn))]
       (when-not (.exists dest) (.createNewFile dest))
       (copy cover dest)))
  ([{:keys [isbn cover]}]
     (add-cover isbn cover)))

(defn get-cover
    ([isbn]
        (.openStream (URL.  (str "http://covers.librarything.com/devkey/c5e0460ed091635d59fbdac846d6680c/medium/isbn/" isbn))))
    ([isbn cover-params]
        (let [cover (:tempfile cover-params)]
            (if (.exists cover)
                cover
                (get-cover isbn)))))
