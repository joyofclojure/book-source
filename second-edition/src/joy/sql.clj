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

(defn process-where-clause [processor expr]
  (str " WHERE " (processor expr)))

(comment

  (process-where-clause shuffle-expr '(AND (< a 5) (< b ~max)))
  ;;=> " WHERE (((a < 5) AND (b < ?)))"
)

(defn process-left-join-clause [processor table on expr]
  (str " LEFT JOIN " table
       " ON " (processor expr)))

(comment
  (apply process-left-join-clause shuffle-expr '(Y :ON (= X.a Y.b)))
  
  ;;=> " LEFT JOIN Y ON (X.a = Y.b)"

  (let [LEFT-JOIN (partial process-left-join-clause shuffle-expr)]
    (LEFT-JOIN 'Y :ON '(= X.a Y.b)))

  ;;=> " LEFT JOIN Y ON (X.a = Y.b)"
)

(defn process-from-clause [processor table & joins]
  (apply str " FROM " table
         (map processor joins)))

(comment

  (process-from-clause shuffle-expr 'X
    (process-left-join-clause shuffle-expr 'Y :ON '(= X.a Y.b)))

  ;;=> " FROM X LEFT JOIN Y ON (X.a = Y.b)"

)

(defn process-select-clause [processor fields & clauses]
  (apply str "SELECT " (str/join ", " fields)
         (map processor clauses)))

(comment

  (process-select-clause shuffle-expr
   '[a b c]
   (process-from-clause shuffle-expr 'X
                        (process-left-join-clause shuffle-expr 'Y :ON '(= X.a Y.b)))
   (process-where-clause shuffle-expr '(AND (< a 5) (< b ~max))))

  ;;=> "SELECT a, b, c FROM X LEFT JOIN Y ON (X.a = Y.b) WHERE ((a < 5) AND (b < ?))"
)

(declare apply-syntax)

(def ^:dynamic *clause-map*
  {'SELECT    (partial process-select-clause apply-syntax)
   'FROM      (partial process-from-clause apply-syntax)
   'LEFT-JOIN (partial process-left-join-clause shuffle-expr)
   'WHERE     (partial process-where-clause shuffle-expr)})

(defn apply-syntax [[op & args]]
  (apply (get *clause-map* op) args))

(defmacro SELECT [& args]
  {:query (apply-syntax (cons 'SELECT args))
   :bindings (vec (for [n (tree-seq coll? seq args)
                        :when (and (coll? n) (= (first n) `unquote))]
                    (second n)))})

(defn query [max]
  (SELECT [a b c]
          (FROM X
                (LEFT-JOIN Y :ON (= X.a Y.b)))
          (WHERE (AND (< a 5) (< b ~max)))))

(comment
  (query 9)
  
  ;;=> {:query "SELECT a, b, c FROM X LEFT JOIN Y ON (X.a = Y.b) WHERE ((a < 5) AND (b < ?))"
  ;;    :bindings [9]}
  
)
