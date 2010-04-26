(ns mcms.covers
  (:import [java.io File]
           [java.net URL])
  (:use [clojure.contrib.duck-streams :only [copy]]
	[clojure.contrib.generic.functor :only [fmap]]
	[mcms opencv]))

(def *covers-dir* "covers/")

(def *library-thing-cover-url* "http://covers.librarything.com/devkey/c5e0460ed091635d59fbdac846d6680c/medium/isbn/")

(defonce *cover-histograms* (atom nil))

(defn- basename [name]
  (let [name (if (instance? String name) name (.getName name))
	dot-index (.lastIndexOf name ".")
	dot-index (if (> 0 dot-index) (.length name) dot-index)]
    (.substring name 0 dot-index)))

(defn do-compare-histograms [needle hay]
  (try (compare-histograms needle hay)
       (catch Throwable e
	 (.printStackTrace e))))

(defn search-cover [cover]
  (let [histogram (compute-histogram (load-image cover))
	pairs (fmap (partial do-compare-histograms histogram) @*cover-histograms*)]
    (sort-by val (filter val pairs))))

(defn read-cover-histograms []
  (let [covers-dir (File. *covers-dir*)
	histogram-files (filter #(-> % (.getName) (.endsWith ".xml")) (.listFiles covers-dir))]
    (reset! *cover-histograms* (into {} (map (juxt basename cv-load-histogram) histogram-files)))))

(defn do-compute-histogram [file]
  (try
    (-> file (load-image) (compute-histogram))
    (catch Throwable e
        (println "Bad coverart" file))))       

(defn compute-cover-histograms []
  (let [histogram-files (filter #(not (-> % (.getName) (.endsWith ".xml"))) (.listFiles (File. *covers-dir*)))]
    (reset! *cover-histograms* (into {} (map (juxt basename do-compute-histogram) histogram-files)))))

(defn save-cover-histogram 
  ([isbn]
     (save-cover-histogram isbn (str *covers-dir* isbn)))
  ([isbn cover]
     (let [histogram (compute-histogram (load-image cover))
	   hist-file (str *covers-dir* isbn ".xml")]
       (cv-save hist-file histogram)
       (swap! *cover-histograms* assoc isbn histogram))))

(defn add-cover 
  ([isbn cover]
     (let [dest (File. (str *covers-dir* isbn))]
       (when-not (.exists dest) (.createNewFile dest))
       (copy cover dest)
       (save-cover-histogram isbn dest)))
  ([{:keys [isbn cover]}]
     (add-cover isbn cover)))

(defn get-cover
    ([isbn]
        (.openStream (URL.  (str *library-thing-cover-url* isbn))))
    ([isbn cover-params]
        (let [cover (:tempfile cover-params)]
            (if (.exists cover)
                cover
                (get-cover isbn)))))

