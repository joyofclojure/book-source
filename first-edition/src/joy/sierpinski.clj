(ns joy.sierpinski
  "Sierpinski squares example from section 3.4")

(defn xors [xs ys]
  (for [x (range xs) y (range ys)]
    [x y (rem (bit-xor x y) 256)]))

(def frame (java.awt.Frame.))
(def gfx (.getGraphics frame))

(defn clear [g] (.clearRect g 0 0 200 200))

(defn f-values [f xs ys]
  (for [x (range xs) y (range ys)]
    [x y (rem (f x y) 256)]))

(defn draw-values [f xs ys]
  (clear gfx)
  (.setSize frame (java.awt.Dimension. xs ys))
  (doseq [[x y v] (f-values f xs ys)]
    (.setColor gfx (java.awt.Color. v v v))
    (.fillRect gfx x y 1 1)))

(comment
  (.setVisible frame true)
  (.setSize frame (java.awt.Dimension. 200 200))
  (.fillRect gfx 100 100 50 75)
  (.setColor gfx (java.awt.Color. 255 128 0))
  (.fillRect gfx 100 150 75 50)

  (draw-values bit-and 256 256)
  (draw-values + 256 256)
  (draw-values * 256 256))
