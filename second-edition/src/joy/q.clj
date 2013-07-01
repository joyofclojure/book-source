(ns joy.q)

(defn rand-ints [n] (take n (repeatedly #(rand-int n))))

(defn sort-parts
  "Lazy, tail-recursive, incremental quicksort.  Works against
       and creates partitions based on the pivot, defined as 'work'."
  [work]
  (lazy-seq
   (loop [[part & parts] work] ;; #: Pull apart work
     (if-let [[pivot & xs] (seq part)]
       (let [smaller? #(< % pivot)] ;; #: Define pivot comparison fn
         (recur (list*
                 (filter smaller? xs)       ;; #: Work all < pivot
                 pivot                      ;; #: Work pivot itself
                 (remove smaller? xs)       ;; #: Work all > pivot
                 parts)))                   ;; #: cancat parts
       (when-let [[x & parts] parts]
         (cons x (sort-parts parts))))))) ;; #: Sort rest if more parts

(defn qsort [xs]
  (sort-parts (list xs)))



(comment

  (rand-ints 10)

  (qsort [2 1 4 3])
  ;;=> (1 2 3 4)

  (qsort (rand-ints 20))

  (take 10 (qsort (rand-ints 10000)))
)