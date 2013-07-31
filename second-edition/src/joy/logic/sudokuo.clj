(ns joy.logic.sudokuofd
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :as logic]
            [clojure.core.logic.fd :as fd]))

(defn all-infd 
  "Assign a domain to all vars."
  [vars domain]
  (if (seq vars)
    (logic/all
      (fd/dom (first vars) domain)
      (all-infd (next vars) domain))
    logic/succeed))

(defn get-square
  "Extract a 3x3 square from grid using
   x y offsets."
  [grid x y]
  (for [x (range x (+ x 3))
        y (range y (+ y 3))]
    (get-in grid [x y])))

(defn init-all
  "Take a vector of [[x y] value] elements.
   Use each element to unify the var at grid 
   location with specified value."
  [grid hints]
  (if (seq hints)
    (let [[location value] (first hints)]
      (logic/all
        (logic/== (get-in grid location) value)
        (init-all grid (next hints))))
    logic/succeed))

(defn sudoku
  "Construct a 9x9 grid populated with fresh logic
   vars. Calculate row, column and square groupings
   of the grid. Use hints to set any initial values.
   Use distincto to ensure that each grouping contains 
   distinct values."
  [hints]
  (let [vars     (repeatedly 81 logic/lvar)
        grid     (->> vars 
                   (partition 9) (map vec) (into []))
        rows     grid
        cols     (apply map vector grid)
        squares  (for [x (range 0 9 3)
                       y (range 0 9 3)]
                   (get-square grid x y))]
    (logic/run-nc 1 [q]
      (logic/== q grid)
      (all-infd vars (fd/domain 1 2 3 4 5 6 7 8 9))
      (init-all grid hints)
      ;; make all rows, columns & squares distinct
      (logic/distincto rows)
      (logic/distincto cols)
      (logic/distincto squares))))

(comment
  ;; ~95ms to solve on my machine
  (sudoku
    [[[0 4] 2] [[0 6] 9] [[1 4] 6] [[1 5] 3]
     [[1 8] 8] [[2 0] 3] [[2 5] 8] [[2 6] 1] 
     [[2 7] 4] [[3 4] 4] [[3 6] 8] [[3 8] 7] 
     [[4 1] 8] [[4 2] 4] [[4 5] 6] [[4 6] 3]])
  )
