(ns joy.logic.sudokuofd
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :as logic]
            [clojure.core.logic.fd :as fd]
            [joy.logic.manual-constraints
             :refer (print-board prep) :as sudoku]))

(defn rowify [board]
  (->> board
       (partition 9)
       (map vec)
       vec))

(defn colify [rows]
  (apply map vector rows))

(defn subgrid [rows]
  (partition 9
    (for [row (range 0 9 3)
          col (range 0 9 3)
          x (range row (+ row 3))
          y (range col (+ col 3))]
      (get-in rows [x y]))))

(defn init [[lv & lvs] [cell & cells]]
  (if lv
    (logic/fresh []
       (if (= '- cell)
         logic/succeed
         (logic/== lv cell))
       (init lvs cells))
    logic/succeed))

(def logic-board #(repeatedly 81 logic/lvar))

(defn solve-logically [board]
  (let [legal-nums (fd/interval 1 9)
        lvars (logic-board)
        rows  (rowify lvars)
        cols  (colify rows)
        grids (subgrid rows)]
    (logic/run 1 [q]
      (init lvars board)                
      (logic/everyg #(fd/in % legal-nums) lvars)         
      (logic/everyg fd/distinct rows)
      (logic/everyg fd/distinct cols)
      (logic/everyg fd/distinct grids)
      (logic/== q lvars))))


(comment
  (log b1)
  
  (def b1 '[3 - - - - 5 - 1 -
            - 7 - - - 6 - 3 -
            1 - - - 9 - - - -
            7 - 8 - - - - 9 -
            9 - - 4 - 8 - - 2
            - 6 - - - - 5 - 1
            - - - - 4 - - - 6
            - 4 - 7 - - - 2 -
            - 2 - 6 - - - - 3])

  (def pn '[- - - - - 6 - - -
            - 5 9 - - - - - 8
            2 - - - - 8 - - -
            - 4 5 - - - - - -
            - - 3 - - - - - -
            - - 6 - - 3 - 5 4
            - - - 3 2 5 - - 6
            - - - - - - - - -
            - - - - - - - - -
            ])
  
  (-> b1
      solve-logically
      first)

  (-> pn
      solve-logically
      first)

  (-> b1
      solve-logically
      first
      sudoku/prep
      sudoku/print-board)

    (-> b1
      sudoku/prep
      sudoku/print-board)

    (logic/run 5 [q]
      (logic/fresh [n]
        (fd/in n (fd/interval 1 Integer/MAX_VALUE))
        (logic/== q n)))

)