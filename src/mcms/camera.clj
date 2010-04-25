(ns mcms.camera
	(:use [mcms opencv])
	(:import [java.awt.event ActionListener KeyAdapter]
             [javax.swing JFrame JLabel Timer]
             [com.sun.jna Native]
             [name.audet.samuel.javacv OpenCVFrameGrabber JavaCvErrorCallback]
		     [name.audet.samuel.javacv.jna cv cxcore cxcore$IplImage cxcore$CvMemStorage cxcore$CvSeq cxcore$CvRect cxcore$CvPoint cv$CvHaarClassifierCascade]))

(set! *warn-on-reflection* true)

(def frame-rate (int 1000/30))

; CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(cvLoad(cascadeName));
; cvCvtColor(grabbedImage, grayImage, CV_BGR2GRAY);
;            CvSeq faces = cvHaarDetectObjects(grayImage, cascade, storage, 1.1, 3, cxcore$CV_HAAR_DO_CANNY_PRUNING);
;            for (int i = 0; i < faces.total; i++) {
;                CvRect r = new CvRect(cvGetSeqElem(faces, i));
;                cvRectangle(grabbedImage, cvPoint(r.x, r.y), cvPoint(r.x+r.width, r.y+r.height), CvScalar.RED, 1, CV_AA, 0);
;                hatPoints[0].x = r.x-r.width/10;    hatPoints[0].y = r.y-r.height/10;
;                hatPoints[1].x = r.x+r.width*11/10; hatPoints[1].y = r.y-r.height/10;
;                hatPoints[2].x = r.x+r.width/2;     hatPoints[2].y = r.y-r.height/2;
;                cvFillConvexPoly(grabbedImage, hatPoints, hatPoints.length, CvScalar.GREEN, CV_AA, 0);
;            }

(defn make-cascade []
  (cv$CvHaarClassifierCascade. (cxcore/cvLoad "haarcascade_frontalface_alt2.xml")))

(def cascade (make-cascade))

(def #^cxcore$CvMemStorage storage (cxcore$CvMemStorage/create))

(defn cv-seq [#^cxcore$CvSeq cvseq]
  (for [i (range (.total cvseq))] (cxcore/cvGetSeqElem cvseq i)))

(defn make-grayscale [#^cxcore$IplImage image]
  (let [gray-image (cxcore$IplImage/create (.width image) (.height image) cxcore/IPL_DEPTH_8U 1)]
    (cv/cvCvtColor image gray-image cv/CV_BGR2GRAY)
    gray-image))

(defn compute-faces [image]
  (let [gray-image (make-grayscale image)
	faces (cv/cvHaarDetectObjects gray-image cascade storage 1.1 3 0)]
    faces))

(defn draw-rect [img #^cxcore$CvRect rect]
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

(defn process-image [#^cxcore$IplImage image]
    (draw-face-rects image (compute-faces image)))

(defn make-grabber []
  (doto (OpenCVFrameGrabber. 0) (.start)))

(defn key-listener [image]
  (proxy [KeyAdapter] [] 
    (keyTyped [e]
      (println "listening!!!")
      #_(println image)
      (-> e (.getSource) (.setVisible false)))))

(defn make-frame [title image]
    (doto (JFrame. title)
      (-> (.getContentPane) (.setLayout (java.awt.GridLayout.)))
      (.add (proxy [JLabel] [] (paint [g] (.drawImage g (.getBufferedImage @image) 0 0 nil))))
      (.addKeyListener (key-listener @image))
      (.show)))     

(defn debug []
  (def  grabber (make-grabber))
  (def image (atom (.grab #^OpenCVFrameGrabber grabber)))
  (def frame (make-frame "Debugging" image)))

(defn end-debug []
  (.stop #^OpenCVFrameGrabber grabber))

(defn capture-action [#^JFrame frame, #^OpenCVFrameGrabber grabber, #^IplImage image]
  (proxy [ActionListener] []
    (actionPerformed [e]
        (if (.isVisible  frame)
          (do
            #_(println (Thread/currentThread))
            (reset! image (.grab grabber))
            (process-image @image)
		    (.clearMem storage)
            (.repaint frame))            
          (do 
            (println "Done!")
            (.stop grabber)
            (-> e (.getSource) (.stop)))))))

(defn main []
  (.redirectError (JavaCvErrorCallback.))
  (Native/setProtected true)
  (let [grabber (make-grabber)
        image (atom (.grab grabber))
        frame (make-frame "Camera Test" image)
        bufferedImage (.getBufferedImage @image)
        timer (Timer. frame-rate (capture-action frame grabber image))]
     (.start timer)
     (.setSize frame (.getWidth bufferedImage) (.getHeight bufferedImage))))

