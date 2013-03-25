(ns joy.units)

(defn convert-unit [context descriptor]
  (reduce +
    (map (fn [[magnitude unit]]
           (let [val (get context unit)]
             (cond (keyword? val) (convert-unit context [magnitude val])
                   (vector? val)  (* magnitude (convert-unit context val))
                   :default       (* magnitude val))))
         (partition 2 descriptor))))

(comment
  (def simple-metric {:meter 1, :km 1000, :cm 1/100, :mm [1/10 :cm]})

  (convert-unit simple-metric [1 :meter])

  ;;=> 1

  (convert-unit simple-metric [50 :cm])

  ;;=> 1/2

  (convert-unit simple-metric [100 :mm])

  ;;=> 1/10

  (convert-unit simple-metric [3 :km 10 :meter 100 :cm])

  ;;=> 3011N
  )
