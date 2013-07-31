(ns joy.unify)

(defn lvar? [x]
  (boolean
   (when (symbol? x)
     (re-matches #"^\?.*" (name x)))))

(lvar? '?x)
;;=> true

(lvar? 2)
;;=> false

(defn satisfy1
  [l r knowledge]
  (let [L (get knowledge l l)
        R (get knowledge r r)]          ;; #1_satisfyone: Look up terms
    (cond
     (= L R)   knowledge                ;; #2_satisfyone: No new knowledge
     (lvar? L) (assoc knowledge L R)    ;; #3_satisfyone: Bind variable to other term!
     (lvar? R) (assoc knowledge R L)
     :default  nil)))


(satisfy1 '?something 2 {})
;;=> {?something 2}

(satisfy1 2 '?something {})
;;=> {?something 2}

(satisfy1 '?something '?something {})

(satisfy1 1 2 {})
;;=> nil

(satisfy1 '?x '?y {})
;;=> {?x ?y}

(satisfy1 '?x 1 (satisfy1 '?x '?y {}))
;;=> {?y 1, ?x ?y}

('?y (satisfy1 '?x 1 (satisfy1 '?x '?y {})))
;;=> 1

(defn satisfy
  [l r knowledge]
  (let [L (get knowledge l l)
        R (get knowledge r r)]
    (cond
     (not knowledge)        nil
     (= L R)          knowledge
     (lvar? L)        (assoc knowledge L R)
     (lvar? R)        (assoc knowledge R L)
     (every? seq? [L R])
       (satisfy (rest L)
                (rest R)
                (satisfy (first L)
                         (first R)
                         knowledge))
     :default nil)))

(satisfy 1 2 {})
;;=> nil

(satisfy '?x '?y {})

(satisfy '(?x 2 3) '(1 2 ?y) {})
;;=> {?y 3, ?x 1}

(satisfy '((((?x)))) '((((2)))) {})
;;=> {?x 2}

(satisfy '(?x 10000 3) '(1 2 ?y) {})
;;=> nil

(satisfy '(?x 2 3 (4 5 ?z)) '(1 2 ?y (4 5 6)) {})
;;=> {?z 6, ?y 3, ?x 1}

(satisfy '?x '?y {})
;;=> {?x ?y}

(satisfy '?x 1 (satisfy '?x '?y {}))
;;=> {?y 1, ?x ?y}

(satisfy '(1 ?x) '(?y (?y 2)) {})
;;=> {?x (?y 2), ?y 1}

('?y (satisfy '?x 1 (satisfy '?x '?y {})))
;;=> 1

(defn ground [binds]
  (into {}
    (map (fn [[k v]]
           [k (loop [v v]
                (if (lvar? v)
                  (recur (binds v))
                  v))])
         binds)))

(ground '{?y 1, ?x ?y})



(require '[clojure.walk :as walk])

(defn subst [term binds]
  (walk/prewalk
   (fn [expr]
     (if (lvar? expr)
       (or (binds expr) expr)
       expr))
   term))

(subst '(1 ?x 3) '{?x 2})
;;=> (1 2 3)

(subst '((((?x)))) '{?x 2})
;;=> ((((2))))

(subst '[1 ?x 3] '{?x 2})
;;=> [1 2 3]

(subst '{:a ?x, :b [1 ?x 3]} '{?x 2})
;;=> {:a 2, :b [1 2 3]}


(def page
  '[:html
    [:head [:title ?title]]
    [:body [:h1 ?title]]])

(subst page '{?title "Hi!"})
;;=> [:html [:head [:title "Hi!"]] [:body [:h1 "Hi!"]]]


(defn meld [term1 term2]
  (->> {}
       (satisfy term1 term2)
       (subst term1)))

(meld '(1 ?x 3) '(1 2 ?y))
;;=> (1 2 3)

(meld '(1 ?x) '(?y (?y 2)))
;;=> (1 (1 2))


(satisfy '?answer 5 {})
;;=> {answer 5}