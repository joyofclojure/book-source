(ns joy.misc
  "Misc. functions and macros from the book.")

(defn join [sep s]
  (apply str (interpose sep s)))

(defn best [f xs]
  (reduce #(if (f % %2) % %2) xs))

(defn strict-map2 [f coll]
  (loop [coll coll, acc []]
    (if (empty? coll)
      acc
      (recur (next coll)
             (conj acc (f (first coll)))))))

(defmethod print-method clojure.lang.PersistentQueue [q, w]
  (print-method '<- w)
  (print-method (seq q) w)
  (print-method '-< w))

(defn index [coll]
  (cond
   (map? coll) (seq coll)
   (set? coll) (map vector coll coll)
   :else (map vector (iterate inc 0) coll)))

(defn pos [pred coll]
  (for [[i v] (index coll) :when (pred v)]
    i))

(defn lz-rec-step [s]
  (lazy-seq
   (if (seq s)
     [(first s) (lz-rec-step (rest s))]
     [])))

(def tri-nums (map triangle (iterate inc 1)))

(def fifth (comp first rest rest rest rest))

;; chapter 7

(defn fnth [n]
  (apply comp
         (cons first (take (dec n) (repeat rest)))))

(defn slope [p1 p2]
  {:pre [(not= p1 p2) (vector? p1) (vector? p2)]
   :post [(float? %)]}
  (/ (- (p2 1) (p1 1)) 
     (- (p2 0) (p1 0))))

(def times-two
  (let [x 2]
    (fn [y] (* y x))))

(def add-and-get
  (let [ai (java.util.concurrent.atomic.AtomicInteger.)]
    (fn [y] (.addAndGet ai y))))

(defn times-n [n]
  (let [x n]
    (fn [y] (* y x))))

(defn divisible [denom]
  (fn [num]
    (zero? (rem num denom))))

(defn filter-divisible [denom s]
  (filter #(zero? (rem % denom))
          s))

(defn pow [base exp]
  (letfn [(kapow [base exp acc]
            (if (zero? exp)
              acc
              (recur base (dec exp) (* base acc))))]
    (kapow base exp 1)))

;; chapter 10

(defmulti what-is class)
(defmethod what-is (Class/forName "[Ljava.lang.String;") [a] "1d String")
(defmethod what-is (Class/forName "[[Ljava.lang.Object;") [a] "2d Object")
(defmethod what-is (Class/forName "[[[[I") [a] "Primitive 4d int")


(defmacro -?> [& forms]
  `(try (-> ~@forms)
        (catch NullPointerException _# nil)))

(def slowly (fn [x] (Thread/sleep 3000) x))
(def sometimes-slowly (manipulable-memoize slowly))


(defn sleeper [s thing] (Thread/sleep (* 1000 s)) thing)

