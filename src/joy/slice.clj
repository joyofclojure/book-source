(ns joy.slice
  "The ISliceable example from section 10.6.1")

(definterface ISliceable
  (slice [^int s ^int e])
  (^int sliceCount []))

(def dumb
  (reify ISliceable
    (slice [_ s e] [:empty])
    (sliceCount [_] 42)))

(defprotocol Sliceable
  (slice [this s e])
  (sliceCount [this]))


(extend ISliceable
  Sliceable
  {:slice (fn [this s e] (.slice this s e))
   :sliceCount (fn [this] (.sliceCount this))})

(defn calc-slice-count [thing]
  "Calculates the number of possible slices using the formula:
      (n + r - 1)!
      ------------
      r!(n - 1)!
   where n is (count thing) and r is 2"
  (let [! #(reduce * (take % (iterate inc 1)))
        n (count thing)]
    (/ (! (- (+ n 2) 1))
       (* (! 2) (! (- n 1))))))

(extend-type String
  Sliceable
  (slice [this s e] (.substring this s (inc e)))
  (sliceCount [this] (calc-slice-count this)))
