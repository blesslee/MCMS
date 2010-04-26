(ns mcms.opencv
  (:import [name.audet.samuel.javacv OpenCVFrameGrabber JavaCvErrorCallback]
	   [javax.swing JFrame JLabel Timer]
	   [name.audet.samuel.javacv.jna
	      cv cv$CvHaarClassifierCascade cv$CvHistogram cv$FloatPointerByReference
	      cxcore cxcore$IplImage cxcore$IplImage$PointerByReference cxcore$CvMemStorage cxcore$CvRect cxcore$CvPoint
	      highgui]
	   [com.sun.jna Memory Native Pointer]
	   [com.sun.jna.ptr FloatByReference]))

(Native/setProtected true)

(defn make-cascade []
  (cv$CvHaarClassifierCascade. (cxcore/cvLoad "haarcascade_frontalface_alt.xml")))

(def cascade (make-cascade))

(def storage (cxcore$CvMemStorage/create))

; Images

(defn get-image-size [image]
  (cxcore/cvGetSize image))

(defn convert-image [image type]
  (let [result (cxcore$IplImage/createCompatible image)]
    (cv/cvCvtColor image result type)
    result))

(defn load-image [file]
  (highgui/cvLoadImage file))

; Histograms

(defn range-array [{:keys [min max]}]
  (let [result (doto (FloatByReference.) (.setPointer (Memory. (* 2 Float/SIZE))))] 
    (.write (.getPointer result) (long 0) (float-array [min max]) (int 0) (int 2))
    result))

#_(defn ranges-array [bins]
  (let [result (doto (cv$FloatPointerByReference.) (.setPointer (Memory. (* 2 Pointer/SIZE))))
	pointer (.getPointer result)
	arrays (doall (map (comp #(.getPointer %) range-array) bins))]
    (.write pointer (long 0) (into-array Pointer arrays) (int 0) (int (count bins)))
    result))

(defn ranges-array [bins]
  (let [arrays (doall (map range-array bins))]
    (cv$FloatPointerByReference. (into-array FloatByReference arrays))))

(defn make-histogram [] 
  (let [bins [{:name "hue", :size 30, :min 0, :max 180}, {:name "saturation", :size 30, :min 0, :max 255}]	
        num-bins (count bins)
	sizes (into-array Integer/TYPE (map :size bins))
	ranges (ranges-array bins)]
    (cv/cvCreateHist num-bins sizes cv/CV_HIST_ARRAY ranges 1)))

(defn compute-histogram! [image & histogram]
  (let [hsv (convert-image image cv/CV_BGR2HSV)
	h (cxcore$IplImage/create (get-image-size image) 8 1)
	s (cxcore$IplImage/create (get-image-size image) 8 1)
	v (cxcore$IplImage/create (get-image-size image) 8 1)
	histogram (or (first histogram) (make-histogram))
	array (cxcore$IplImage$PointerByReference. (into-array cxcore$IplImage [h s]))]
    (cxcore/cvSplit image h s v nil)
    (cv/cvCalcHist array histogram 0 nil)))

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

(defn make-grabber []
  (doto (OpenCVFrameGrabber. 0) (.start)))

(defn make-frame [title image key-listener]
    (doto (JFrame. title)
      (-> (.getContentPane) (.setLayout (java.awt.GridLayout.)))
      (.add (proxy [JLabel] [] (paint [g] (.drawImage g (.getBufferedImage @image) 0 0 nil))))
      (.addKeyListener key-listener)
      (.show)))