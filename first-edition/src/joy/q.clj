(ns joy.q
  "Lazy, tail-recursive quick-sort implementation from section 6.4")


(defn nom [n] (take n (repeatedly #(rand-int n))))

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
