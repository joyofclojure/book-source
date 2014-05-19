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


;; chapter 11

(defn sleeper [s thing] (Thread/sleep (* 1000 s)) thing)


;; chapter 12

(defn ^Float asum-sq [^floats xs]
  (let [^floats dbl (amap xs i ret
                  (* (aget xs i)
                     (aget xs i)))]
    (areduce dbl i ret 0
             (+ ret (aget dbl i)))))

(defn zencat1 [x y]
  (loop [src y, ret x]
    (if src
      (recur (next src) (conj ret (first src)))
      ret)))
    
(defn zencat2 [x y]
  (loop [src y, ret (transient x)]                  ;; #: Create transient
    (if src
      (recur (next src) (conj! ret (first src)))    ;; #: Use transient conj!
      (persistent! ret))))                          ;; #: Return persistent


(defn seq1 [s]
  (lazy-seq
   (when-let [[x] (seq s)]
     (cons x (seq1 (rest s))))))

(defn mean
  "Takes a sequence of integers and returns their mean value"
  [sq]
  (let [length (int (count sq))]
    (if (zero? length)
      0
      (/ (int (reduce + sq)) length))))


;; chapter 13

(defn with-redefs-fn [binding-map func & args]
  (let [root-bind (fn [m]
                    (doseq [[a-var a-val] m] (.bindRoot a-var a-val)))
        old-vals (zipmap (keys binding-map)
                         (map deref (keys binding-map)))]
    (try
      (root-bind binding-map)
      (apply func args)
      (finally
       (root-bind old-vals)))))

(defmacro with-redefs [bindings & body]
  `(with-redefs-fn ~(zipmap (map #(list `var %) (take-nth 2 bindings))
                            (take-nth 2 (next bindings)))
     (fn [] ~@body)))


(defmacro defformula [nm bindings & formula]
  `(let ~bindings
     (let [formula#   (agent ~@formula) ;; #: Create formula as Agent
           update-fn# (fn [key# ref# o# n#] 
                        (send formula# (fn [_#] ~@formula)))]
       (doseq [r# ~(vec (map bindings (range 0 (count bindings) 2)))]
         (add-watch r# :update-formula update-fn#)) ;; #: Add a watch to each reference
       (def ~nm formula#))))
    
(def h (ref 25))
(def ab (ref 100))
    
(defformula avg [at-bats ab hits h] ;; #: Create baseball formula
  (float (/ @hits @at-bats)))
