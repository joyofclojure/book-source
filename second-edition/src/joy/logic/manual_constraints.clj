(ns joy.logic.manual-constraints
  (:require [clojure.set :as set]))

(def b1 '[3 - - - - 5 - 1 -
          - 7 - - - 6 - 3 -
          1 - - - 9 - - - -
          7 - 8 - - - - 9 -
          9 - - 4 - 8 - - 2
          - 6 - - - - 5 - 1
          - - - - 4 - - - 6
          - 4 - 7 - - - 2 -
          - 2 - 6 - - - - 3])

(def b2 '[5 3 - - 7 - - - -
          6 - - 1 9 5 - - -
          - 9 8 - - - - 6 -
          8 - - - 6 - - - 3
          4 - - 8 - 3 - - 1
          7 - - - 2 - - - 6
          - 6 - - - - 2 8 -
          - - - 4 1 9 - - 5
          - - - - 8 - - 7 9])

(defn rows [board sz]
  (partition sz board))

(defn row-for [board index sz]
  (nth (rows board sz) (/ index 9)))

(defn column-for [board index sz]
  (let [col (mod index sz)]
    (map #(nth % col)
         (rows board sz))))

(defn subgrid-for [board i]
  (let [rows (rows board 9)
        gc (/ (mod i 9) 3)
        gr (/ (/ i 9) 3)
        grp-col (column-for (mapcat #(partition 3 %) rows) gc 3)
        grp (take 3 (drop (* 3 (int gr)) grp-col))]
    (flatten grp)))

(defn numbers-present-for [board i]
  (set
   (concat (row-for board i 9)
           (column-for board i 9)
           (subgrid-for board i))))

(defn possible-placements [board index]
  (set/difference #{1 2 3 4 5 6 7 8 9}
                  (numbers-present-for board index)))

(defn index [coll]
  (cond
   (map? coll) (seq coll)
   (set? coll) (map vector coll coll)
   :else (map vector (iterate inc 0) coll)))

(defn pos [pred coll]
  (for [[i v] (index coll) :when (pred v)] i))

(defn solve
  [board]
  (if-let [[i & _] (and (some '#{-} board)
                        (pos '#{-} board))]
    (flatten (map #(solve (assoc board i %))
                  (possible-placements board i)))
    board))

(defn print-board [board]
  (let [row-sep (apply str (repeat 37 "-"))]
    (println row-sep)
    (dotimes [row (count board)]
      (print "| ")
      (doseq [subrow (nth board row)]
        (doseq [cell (butlast subrow)]
          (print (str cell "   ")))
        (print (str (last subrow) " | ")))
      (println)
      (when (zero? (mod (inc row) 3))
        (println row-sep)))))

(defn prep [board]
  (map #(partition 3 %)
       (partition 9 board)))

(comment
  (-> b1
      solve
      prep
      print-board)

  (-> b1
      prep)
  
  (-> b2
      solve
      prep
      print-board)

  (-> b1 prep print-board)
)
