(ns mcms.opencv
    (:use [clj-native.direct :only [defclib loadlib typeof]]
        [clj-native.structs :only [byref byval]]
        [clj-native.callbacks :only [callback]])
    (:import [highgui HighGuiLibrary]))
        

(defn create-capture []
  (HighGuiLibrary/cvCreateCameraCapture HighGuiLibrary/CV_CAP_ANY))

(defn get-pixels []
    )
    
(defn grab-frame [capture]
    (HighGuiLibrary/cvQueryFrame capture))
    
(defn diff [image1 image2]
    (HighGuiLibrary/cvAbsDiff image1 image2 image2))
    
(defn convert [output-type]
    (.convert output-type))
    
(defn blur [blur-type amount]
    (.blur blur-type amount))
    
(defn threshold [floor]
    (.threshold floor))
    
(defn remember []
    (.remember))