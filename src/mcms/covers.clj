(ns mcms.covers
  (:import [java.io File])
  (:use [clojure.contrib.duck-streams :only [copy]]))

(defn add-cover 
  ([isbn cover]
     (let [dest (File. (str "./covers/" isbn))]
       (when-not (.exists dest) (.createNewFile dest))
       (copy cover dest)))
  ([{:keys [isbn cover]}]
     (add-cover isbn cover)))

