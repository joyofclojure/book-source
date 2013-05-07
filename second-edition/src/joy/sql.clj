(ns joy.sql
  (:use [clojure.string :as str :only []]))

(defn shuffle-expr [expr]
  (if (coll? expr)
    (if (= (first expr) `unquote)
      "?"
      (let [[op & args] expr]
        (str "(" (str/join (str " " op " ")
                           (map shuffle-expr args)) ")")))
    expr))

(comment

  (shuffle-expr 42)
  
  (shuffle-expr '(= X.a Y.b))

  (shuffle-expr '(AND (< a 5) (< b ~max)))

  (shuffle-expr '(AND (< a 5) (OR (> b 0) (< b ~max))))

  (shuffle-expr `(unquote max))

  (let [max 42]
    `(unquote ~max))


)

(defn process-where-clause [expr]
  (str " WHERE " (shuffle-expr expr)))

(def WHERE process-where-clause)

(comment

  (process-where-clause '(AND (< a 5) (< b ~max)))
  ;;=> " WHERE (((a < 5) AND (b < ?)))"

  (process-where-clause '(AND (< a 5) (< b ~max)))

)

(defn process-left-join-clause [table on expr]
  (str " LEFT JOIN " table
       " ON " (shuffle-expr expr)))

(def LEFT-JOIN process-left-join-clause)

(comment
  (apply process-left-join-clause '(Y :ON (= X.a Y.b)))

  (LEFT-JOIN 'Y :ON '(= X.a Y.b))
)

(declare expand-clause)

(defn process-from-clause [table & joins]
  (apply str " FROM " table
         (map shuffle-expr joins)))

(def FROM process-from-clause)

(comment

  (FROM 'X (LEFT-JOIN 'Y :ON '(= X.a Y.b)))

  ;;=> " FROM X LEFT JOIN Y ON (X.a = Y.b)"

)

(defn process-select-clause [fields & clauses]
  (apply str "SELECT " (str/join ", " fields)
         (map shuffle-expr clauses)))

(def SELECT process-select-clause)

(comment

  (SELECT
   '[a b c]
   (FROM 'X (LEFT-JOIN 'Y :ON '(= X.a Y.b)))
   )

)

(def clause-map
  {'SELECT    process-select-clause
   'FROM      process-from-clause
   'LEFT-JOIN process-left-join-clause
   'WHERE     process-where-clause})

(defn expand-clause [[op & args]]
  (apply (clause-map op) args))

(defmacro SELECT [& args]
  [(expand-clause (cons 'SELECT args))
   (vec (for [n (tree-seq coll? seq args)
              :when (and (coll? n) (= (first n) `unquote))]
          (second n)))])

(defn query [max]
  (SELECT [a b c]
          (FROM X
                (LEFT-JOIN Y :ON (= X.a Y.b)))
          (WHERE (AND (< a 5) (< b ~max)))))

;; (query 9)
;;=> ["SELECT a, b, c FROM X LEFT JOIN Y ON (X.a = Y.b) WHERE ((a < 5) AND (b < ?))" [9]]

