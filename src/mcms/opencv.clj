(ns mcms.opencv
  (:import [name.audet.samuel.javacv OpenCVFrameGrabber JavaCvErrorCallback]
	   [javax.swing JFrame JLabel Timer]
	   [java.io File]
	   [javax.imageio ImageIO]
	   [name.audet.samuel.javacv.jna
	      cv cv$CvHaarClassifierCascade cv$CvHistogram cv$CvHistogram$PointerByReference cv$FloatPointerByReference
	      cxcore cxcore$IplImage cxcore$IplImage$PointerByReference cxcore$CvMemStorage cxcore$CvRect cxcore$CvPoint cxcore$CvAttrList
	      highgui]
	   [com.sun.jna Memory Native Pointer]
	   [com.sun.jna.ptr FloatByReference]))

(Native/setProtected true)

; Persistence

(defn cv-save [filename obj]
  (cxcore/cvSave filename (.getPointer obj) nil nil (.byValue (cxcore$CvAttrList.))))

(defn cv-save-to-string [obj]
  (let [file (File/createTempFile "mcms-tmp-" ".xml")
	name (.getCanonicalPath file)]
    (cv-save name obj)
    (.deleteOnExit file)
    (slurp name)))

(defmulti cv-load class)

(defmethod cv-load File [file]
  (cxcore/cvLoad (.getCanonicalPath file)))

(defmethod cv-load String [filename]
  (cxcore/cvLoad filename))

(defn cv-load-histogram [file]
  (cv$CvHistogram. (cv-load file)))

(defn- make-cascade []
  (cv$CvHaarClassifierCascade. (cv-load "haarcascade_frontalface_alt.xml")))

(def cascade (make-cascade))

(def storage (cxcore$CvMemStorage/create))

; Images

(defn get-image-size [image]
  (cxcore/cvGetSize image))

(defn convert-image [image type & [result & rest]]
  (let [result (or result (cxcore$IplImage/createCompatible image))]
    (cv/cvCvtColor image result type)
    result))

(defmulti load-image class)

(defmethod load-image String [file]
  (highgui/cvLoadImage file))

(defmethod load-image File [file]
  (let [image (ImageIO/read file)]
    (cxcore$IplImage/createFrom image)))

; Histograms

(defn- range-array [{:keys [min max]}]
  (let [result (doto (FloatByReference.) (.setPointer (Memory. (* 2 Float/SIZE))))] 
    (.write (.getPointer result) (long 0) (float-array [min max]) (int 0) (int 2))
    result))

(defn- ranges-array [bins]
  (let [arrays (doall (map range-array bins))]
    (cv$FloatPointerByReference. (into-array FloatByReference arrays))))

(defn- make-histogram [& [bins & rest]] 
  (let [bins (when-not bins [{:name "hue", :size 30, :min 0, :max 180}, {:name "saturation", :size 30, :min 0, :max 255}])	
        num-bins (count bins)
	sizes (into-array Integer/TYPE (map :size bins))
	ranges (ranges-array bins)]
    (cv/cvCreateHist num-bins sizes cv/CV_HIST_ARRAY ranges 1)))

(defn compare-histograms [hist1 hist2] 
  (cv/cvCompareHist hist1 hist2 cv/CV_COMP_BHATTACHARYYA))

(defn normalize-histogram! [histogram]
  (cv/cvNormalizeHist histogram, 1.0))

(defn compute-histogram [image]
  (let [hsv (convert-image image cv/CV_BGR2HSV)
	plane1 (cxcore$IplImage/create (get-image-size image) 8 1)
	plane2 (cxcore$IplImage/create (get-image-size image) 8 1)
	plane3 (cxcore$IplImage/create (get-image-size image) 8 1)
	histogram (make-histogram)
	array (cxcore$IplImage$PointerByReference. (into-array cxcore$IplImage [plane1 plane2]))]
    (cxcore/cvSplit hsv plane1 plane2 plane3 nil)
    (cv/cvCalcHist array histogram 0 nil)
    (normalize-histogram! histogram)
    histogram))


; Faces

(defn cv-seq [cvseq]
  (for [i (range (.total cvseq))] (cxcore/cvGetSeqElem cvseq i)))

(defn compute-faces [image]
  (let [gray-image (convert-image image cv/CV_BGR2GRAY (cxcore$IplImage/create (.width image) (.height image) cxcore/IPL_DEPTH_8U 1))
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