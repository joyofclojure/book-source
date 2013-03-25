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

  (convert simple-metric [1 :meter])

  ;;=> 1

  (convert simple-metric [50 :cm])

  ;;=> 1/2

  (convert simple-metric [100 :mm])

  ;;=> 1/10

  (convert simple-metric [3 :km 10 :meter 100 :cm])

  ;;=> 3011N
)
