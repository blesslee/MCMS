(ns mcms.camera
  	(:use [mcms opencv])
	(:import [name.audet.samuel.javacv CanvasFrame OpenCVFrameGrabber JavaCvErrorCallback]))

;(set! *warn-on-reflection* true)

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
    (loop [image (.grab grabber)]
      (if (.isVisible frame)
	(do
	  (process-image frame image)
	  (.clearMem storage)
	  (recur (.grab grabber)))
	(.stop grabber)))))

