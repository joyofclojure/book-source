(ns joy.bots
  "The bots example demonstrating closures from section 7.2")

(def bearings [{:x  0, :y  1}           ; north
               {:x  1, :y  0}           ; east
               {:x  0, :y -1}           ; south
               {:x -1, :y  0}])         ; west


(defn forward [x y bearing-num]
  [(+ x (:x (bearings bearing-num)))
   (+ y (:y (bearings bearing-num)))])


(defn bot [x y bearing-num]
  {:coords     [x y]
   :bearing    ([:north :east :south :west] bearing-num)
   :forward    (fn [] (bot (+ x (:x (bearings bearing-num)))
                           (+ y (:y (bearings bearing-num)))
                           bearing-num))
   :turn-right (fn [] (bot x y (rem (+ 1 bearing-num) 4)))
   :turn-left  (fn [] (bot x y (rem (+ 3 bearing-num) 4)))})


(defn mirror-bot [x y bearing-num]
  {:coords     [x y]
   :bearing    ([:north :east :south :west] bearing-num)
   :forward    (fn [] (mirror-bot (- x (:x (bearings bearing-num)))
                                  (- y (:y (bearings bearing-num)))
                                  bearing-num))
   :turn-right (fn [] (mirror-bot x y (rem (+ 3 bearing-num) 4)))
   :turn-left  (fn [] (mirror-bot x y (rem (+ 1 bearing-num) 4)))})

