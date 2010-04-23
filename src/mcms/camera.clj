(ns mcms.camera
	(:use [mcms opencv])
	(:import [name.audet.samuel.javacv CanvasFrame OpenCVFrameGrabber JavaCvErrorCallback]
		 [name.audet.samuel.javacv.jna cv cxcore cxcore$IplImage cxcore$CvMemStorage cxcore$CvRect cxcore$CvPoint cv$CvHaarClassifierCascade]))

;(set! *warn-on-reflection* true)

; CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(cvLoad(cascadeName));
; cvCvtColor(grabbedImage, grayImage, CV_BGR2GRAY);
;            CvSeq faces = cvHaarDetectObjects(grayImage, cascade, storage, 1.1, 3, 0/*CV_HAAR_DO_CANNY_PRUNING*/);
;            for (int i = 0; i < faces.total; i++) {
;                CvRect r = new CvRect(cvGetSeqElem(faces, i));
;                cvRectangle(grabbedImage, cvPoint(r.x, r.y), cvPoint(r.x+r.width, r.y+r.height), CvScalar.RED, 1, CV_AA, 0);
;                hatPoints[0].x = r.x-r.width/10;    hatPoints[0].y = r.y-r.height/10;
;                hatPoints[1].x = r.x+r.width*11/10; hatPoints[1].y = r.y-r.height/10;
;                hatPoints[2].x = r.x+r.width/2;     hatPoints[2].y = r.y-r.height/2;
;                cvFillConvexPoly(grabbedImage, hatPoints, hatPoints.length, CvScalar.GREEN, CV_AA, 0);
;            }

(defn make-cascade []
  (cv$CvHaarClassifierCascade. (cxcore/cvLoad "haarcascade_frontalface_alt.xml")))

(def cascade (make-cascade))

(def storage (cxcore$CvMemStorage/create))

(defn cv-seq [cvseq]
  (for [i (range (.total cvseq))] (cxcore/cvGetSeqElem cvseq i)))

(defn make-grayscale [image]
  (let [gray-image (cxcore$IplImage/create (.width image) (.height image) cxcore/IPL_DEPTH_8U 1)]
    (cv/cvCvtColor image gray-image cv/CV_BGR2GRAY)
    gray-image))

(defn compute-faces [image]
  (let [gray-image (make-grayscale image)
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
  (map (partial draw-rect img) rects))

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

(defn make-frame [title]
  (CanvasFrame. title))

(defn debug []
  (def grabber (make-grabber))
  (def frame (make-frame "Debugging"))
  (def image (.grab grabber)))

(defn end-debug []
  (.stop grabber)
  (.dispose frame))

(defn main []
  (.redirectError (JavaCvErrorCallback.))
  (let [frame (make-frame "Camera Test")
	grabber (make-grabber)]
	(loop 	[image (.grab grabber)]
		(if (.isVisible frame)
		  (do
		    (process-image frame image)
		    (.clearMem storage)
		    (recur (.grab grabber)))
		  (do
		    (.stop grabber)
		    (.dispose frame))))))

