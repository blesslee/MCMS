(ns mcms.entrez
(:require [clojure.xml :as xml]
[clojure.zip :as zip]
[clojure.contrib.zip-filter.xml :as zf]))

;librarything - get: work(author, title)
(defn get-work [isbn] (zip/xml-zip (xml/parse (str "http://www.librarything.com/services/rest/1.0/?method=librarything.ck.getwork&isbn=" isbn "&apikey=c5e0460ed091635d59fbdac846d6680c"))))
(defn get-author [isbn] (first (zf/xml->  (get-work isbn) :ltml :item :author zf/text)))
(defn get-title [isbn] (first (zf/xml-> (get-work isbn) :ltml :item :commonknowledge :fieldList :field [(zf/attr= :name "canonicaltitle")] :versionList :version [(zf/attr= :archived "0")] :factList :fact zf/text)))

;google - ?



 

