(ns mcms.camera
  (:use [mcms opencv])
  (:import [java.awt.event ActionListener KeyAdapter]
	   [javax.swing JFrame JLabel Timer]
	   [name.audet.samuel.javacv OpenCVFrameGrabber]))

;(set! *warn-on-reflection* true)

(def frame-rate (int 1000/30))

(defn process-image [#^cxcore$IplImage image]
    (draw-face-rects image (compute-faces image)))

(defn key-listener [image]
  (proxy [KeyAdapter] [] 
    (keyTyped [e]
      (println "listening!!!")
      #_(println image)
      (-> e (.getSource) (.setVisible false)))))     

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
  (let [grabber (make-grabber)
        image (atom (.grab grabber))
        frame (make-frame "Camera Test" image (key-listener @image))
        bufferedImage (.getBufferedImage @image)
        timer (Timer. frame-rate (capture-action frame grabber image))]
     (.start timer)
     (.setSize frame (.getWidth bufferedImage) (.getHeight bufferedImage))))

