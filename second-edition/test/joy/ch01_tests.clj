(ns joy.ch01-tests
  (:use clojure.test joy.sql))

(deftest general-tests
  (testing "that the snippets in chpater 1 works as expected"))


(for [x [:a :b], y (range 5) :when (odd? y)] [x y])
;=> ([:a 1] [:a 3] [:b 1] [:b 3])


(doseq [x [:a :b], y (range 5) :when (odd? y)] (prn x y))
; :a 1
; :a 3
; :b 1
; :b 3
;=> nil


(for [x [:a :b], y (range 5) :when (odd? y)] [x y])
;=> ([:a 1] [:a 3] [:b 1] [:b 3])
(doseq [x [:a :b], y (range 5) :when (odd? y)] (prn x y))
; :a 1
; :a 3
; :b 1
; :b 3
;=> nil


(defn query [max]
  (SELECT [a b c]
    (FROM X
      (LEFT-JOIN Y :ON (= X.a Y.b)))
    (WHERE (AND (< a 5) (< b ~max)))))


(defn query [max]
  (SELECT [a b c]
    (FROM X
      (LEFT-JOIN Y :ON (= X.a Y.b)))
    (WHERE (AND (< a 5) (< b ~max)))))


(query 5)
;=> ["SELECT a, b, c FROM X LEFT JOIN Y ON (X.a = Y.b)
         WHERE ((a < 5) AND (b < ?))"
        [5]]


(query 5)
;=> ["SELECT a, b, c FROM X LEFT JOIN Y ON (X.a = Y.b)
         WHERE ((a < 5) AND (b < ?))"
        [5]]


(SELECT [a b c]
  (FROM X
        (LEFT-JOIN Y :ON (= X.a Y.b)))
  (WHERE (AND (< a 5) (< b ~max))))

;=> ["SELECT a, b, c FROM X LEFT JOIN Y ON (X.a = Y.b) WHERE ((a < 5) AND (b < ?))" 
;; [#]]


(SELECT [a b c]
  (FROM X
        (LEFT-JOIN Y :ON (= X.a Y.b)))
  (WHERE (AND (< a 5) (< b ~max))))

;=> ["SELECT a, b, c FROM X LEFT JOIN Y ON (X.a = Y.b) WHERE ((a < 5) AND (b < ?))" 
;; [#]]


(defn example-query []
  (SELECT [a b c]
    (FROM X
          (LEFT-JOIN Y :ON (= X.a Y.b)))
    (WHERE (AND (< a 5) (< b ~max)))))

(example-query 9)
;=> ["SELECT a, b, c 
;;    FROM X LEFT JOIN Y ON (X.a = Y.b) 
;;    WHERE ((a < 5) AND (b < ?))" 
;;    [9]]


(defn example-query []
  (SELECT [a b c]
    (FROM X
          (LEFT-JOIN Y :ON (= X.a Y.b)))
    (WHERE (AND (< a 5) (< b ~max)))))

(example-query 9)
;=> ["SELECT a, b, c 
;;    FROM X LEFT JOIN Y ON (X.a = Y.b) 
;;    WHERE ((a < 5) AND (b < ?))" 
;;    [9]]

(defprotocol Concatenatable
  (cat [this other]))
(extend-type String
  Concatenatable
  (cat [this other]
    (.concat this other)))
(cat "House" " of Leaves")
;=> "House of Leaves"


(extend-type java.util.List
  Concatenatable
  (cat [this other]
    (concat this other)))
(cat [1 2 3] [4 5 6])
;=> (1 2 3 4 5 6)


(extend-type java.util.List
  Concatenatable
  (cat [this other]
    (concat this other)))
(cat [1 2 3] [4 5 6])
;=> (1 2 3 4 5 6)

(def ^:dynamic *file-key* \a)
(def ^:dynamic *rank-key* \0)

(defn- file-component [file]
  (- (int file) (int *file-key*)))                                                          

(defn- rank-component [rank]
  (->> (int *rank-key*)       
       (- (int rank))
       (- 8)
       (* 8)))

(defn- index [file rank]
  (+ (file-component file) (rank-component rank)))                                          

(defn lookup [board pos]
  (let [[file rank] pos]
    (board (index file rank))))

(lookup (initial-board) "a1")
;=> \R

(letfn [(index [file rank]
          (let [f (- (int file) (int \a))
                r (* 8 (- 8 (- (int rank) (int \0))))]
            (+ f r)))]
  (defn lookup [board pos]
    (let [[file rank] pos]
      (board (index file rank)))))

(defn lookup2 [board pos]
  (let [[file rank] (map int pos)
        [fc rc]     (map int [\a \0])
        f (- file fc)
        r (* 8 (- 8 (- rank rc)))
        index (+ f r)]
    (board index)))
