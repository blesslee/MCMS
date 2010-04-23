(ns mcms.opencv
    (:import	[java.util.zip ZipFile ZipEntry]
	 	[name.audet.samuel.javacv.jna highgui cxcore cv cxcore$IplImage]
))

(defn create-capture []
  (highgui/cvCreateCameraCapture highgui/CV_CAP_ANY))

(defn get-pixels []
    )
    
(defn grab-frame [capture]
    (highgui/cvQueryFrame capture))
    
#_(defn diff [image1 image2]
    (highgui/cvAbsDiff image1 image2 image2))
    
(defn convert [output-type]
    (.convert output-type))
    
(defn blur [blur-type amount]
    (.blur blur-type amount))
    
(defn threshold [floor]
    (.threshold floor))
    
#_(defn remember []
    (.remember))
