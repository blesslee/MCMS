(ns mcms.opencv
  (:import [name.audet.samuel.javacv CanvasFrame OpenCVFrameGrabber JavaCvErrorCallback]
	   [name.audet.samuel.javacv.jna
	      cv cv$CvHaarClassifierCascade cv$CvHistogram
	      cxcore cxcore$IplImage cxcore$CvMemStorage cxcore$CvRect cxcore$CvPoint
	      highgui]
	   [com.sun.jna.ptr FloatByReference]))

(defn make-cascade []
  (cv$CvHaarClassifierCascade. (cxcore/cvLoad "haarcascade_frontalface_alt.xml")))

(def cascade (make-cascade))

(def storage (cxcore$CvMemStorage/create))

; Images

(defn get-image-size [image]
  (cxcore/cvGetSize image))

(defn convert-image [image type]
  (let [result (cxcore$IplImage/create (get-image-size) cxcore/IPL_DEPTH_8U 1)]
    (cv/cvCvtColor image result type)
    result))

(defn load-image [file]
  (highgui/cvLoadImage file))

(defn make-frame [title]
  (CanvasFrame. title))

; Histograms

(defn param-array 
  ([params key]
     (into-array (map key params)))
  ([params key type]
     (into-array type (map key params))))

(defn make-histogram [] 
  (let [bins [{:name "hue", :size 30, :min 0, :max 180}, {:name "saturation", :size 30, :min 0, :max 255}]
        num-bins (count bins)
	ranges (into-array FloatByReference (map #(FloatByReference. %) (mapcat (juxt :min :max) bins)))]
    (cv$CvHistogram/create (count bins) (param-array bins :size Integer/TYPE) cv/CV_HIST_ARRAY ranges 1)))

(defn compute-histogram! [image & histogram]
  (let [hsv (cxcore$IplImage/create (get-image-size image) 8 3)
	h (cxcore$IplImage/create (get-image-size image) 8 1)
	s (cxcore$IplImage/create (get-image-size image) 8 1)
	v (cxcore$IplImage/create (get-image-size image) 8 1)
	histogram (if histogram histogram (make-histogram))]
    (convert-image image hsv cv/CV_BGR2HSV)
    (cxcore/cvSplit image h s v 0)
    (cv/cvCalcHist (into-array (.pointerByReference h) (.pointerByReference s)) histogram 0 0)))

(defn normalize-histogram [histogram]
  (cv/cvNormalizeHist histogram, 1.0))

; Faces

(defn cv-seq [cvseq]
  (for [i (range (.total cvseq))] (cxcore/cvGetSeqElem cvseq i)))

(defn compute-faces [image]
  (let [gray-image (convert-image image cv/CV_BGR2GRAY)
	faces (cv/cvHaarDetectObjects gray-image cascade storage 1.1 3 0)]
    faces))

(defn draw-rect [img rect]
  (let [pt1 (cxcore$CvPoint.)
	pt2 (cxcore$CvPoint.)
        x (.x rect)
        y (.y rect)
        width (.width rect)
        height (.height rect)]
    (set! (.x pt1) x)
    (set! (.y pt1) y)
    (set! (.x pt2) (+ x width))
    (set! (.y pt2) (+ y height))
    (cxcore/cvRectangle img (.byValue pt1) (.byValue pt2) (cxcore/CV_RGB 255 0 0) 3 8 0))) 

(defn draw-rects [img rects]
  (dorun (map (partial draw-rect img) rects)))

(defn face->rect [face]
  (cxcore$CvRect. face))

(defn draw-face-rects [image faces]
  (let [fseq (cv-seq faces)]
    (println (count fseq))
    (draw-rects image (map face->rect fseq))))

(defn process-image [#^CanvasFrame frame #^cxcore$IplImage image]
    (draw-face-rects image (compute-faces image))
    (.showImage frame image))

(defn make-grabber []
  (doto (OpenCVFrameGrabber. 0) (.start)))
