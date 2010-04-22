(ns mcms.face-detect
  (:import (javax.swing JFrame JLabel Timer)
	   (java.awt.event ActionListener KeyAdapter)
	   (java.awt Canvas Image Color)
	   (java.awt.image MemoryImageSource)
	   (hypermedia.video OpenCV)))

(def frame-rate (int 1000/30))
(def width 640)
(def height 480)

(defn vision []
  (doto (OpenCV.)
    (.capture width height)
    (.cascade OpenCV/CASCADE_FRONTALFACE_ALT)))

(defn capture-image [vis]
  (.read vis)
  (let [mis (MemoryImageSource. (.width vis) (.height vis)
				(.pixels vis) 0 (.width vis))]
    (.createImage (Canvas.) mis)))

(defn detect-face [vis]
  (.detect vis 1.2 2 OpenCV/HAAR_DO_CANNY_PRUNING 20 20))

(defn capture-action [vis panel image faces]
  (proxy [ActionListener] []
    (actionPerformed
     [e]
     (dosync 
        (ref-set image (capture-image vis))
        (ref-set faces (detect-face vis)))
     (.repaint panel))))

(defn panel [image faces]
  (proxy [JLabel] [] 
    (paint
     [g]
     (.drawImage g @image 0 0 nil)
     (.setColor g Color/red)
     (doseq [square @faces]
       (.drawRect g
		  (.x square) (.y square)
		  (.width square) (.height square))))))

(defn key-listener [vis timer]
  (proxy [KeyAdapter] [] 
    (keyReleased 
     [e]
     (.stop timer)
     (.dispose vis))))

(defn main []
  (let [vis   (vision)
	image (ref (capture-image vis))
	faces (ref (detect-face vis))
	panel (panel image faces)
	timer (Timer. frame-rate (capture-action vis panel image faces))]
    (.start timer)
    (doto (JFrame.)
      (.add panel)
      (.addKeyListener (key-listener vis timer))
      (.setSize width height)
      (.show))))

