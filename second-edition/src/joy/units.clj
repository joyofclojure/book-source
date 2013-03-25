(ns joy.units)

(defn convert [context descriptor]
  (reduce +
    (map (fn [[mag unit]]
           (let [val (get context unit)]
             (cond (keyword? val) (convert context [mag val])
                   (vector? val)  (* mag (convert context val))
                   :default       (* mag val))))
         (partition 2 descriptor))))

(comment
  (def simple-metric {:meter 1, :km 1000, :cm 1/100, :mm [1/10 :cm]})


  ;; how many meters are in 3 kilometers, 10 meters, 80 centimeters, 10mm?
  (->    (* 3  (:km simple-metric))
      (+ (* 10 (:meter simple-metric)))
      (+ (* 80 (:cm simple-metric)))
      (+ (* (:cm simple-metric)
            (* 10 (first (:mm simple-metric)))))
      float)

  (convert simple-metric [1 :meter])

  ;;=> 1

  (convert simple-metric [50 :cm])

  ;;=> 1/2

  (convert simple-metric [100 :mm])

  ;;=> 1/10

  (float (convert simple-metric [3 :km 10 :meter 80 :cm 10 :mm]))

  ;;=> 3011N
)
