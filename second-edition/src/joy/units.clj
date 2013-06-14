(ns joy.units)

(defn convert [context descriptor]
  (reduce (fn [result [mag unit]]
            (+ result
               (let [val (get context unit)]
                 (if (vector? val)
                   (* mag (convert context val))
                   (* mag val)))))
          0
          (partition 2 descriptor)))


(def distance-reader
  (partial convert
           {:m 1
            :km 1000,
            :cm 1/100,
            :mm [1/10 :cm]}))


(def time-reader
  (partial convert
           {:sec 1
            :min 60,
            :hr  [60 :min],
            :day [24 :hr]}))


(comment

  (read-string "#unit/length [1 :m]")
  ;;=> 1
  
  (binding [clojure.core/*data-readers* {'unit/time #'joy.units/time-reader}]
    (read-string "#unit/time [1 :min 30 :sec]"))

  ;;=> 90

  (binding [*default-data-reader-fn* #(-> {:tag %1 :payload %2})]
    (read-string "#nope [:doesnt-exist]"))
)




(defn relative-units [context unit]
  (if-let [spec (get context unit)]
    (if (vector? spec)
      (convert context spec)
      spec)
    (throw (RuntimeException. (str "Undefined unit " unit)))))


(defmacro defunits-of [name base-unit & conversions]
  (let [magnitude (gensym)
        unit (gensym)
        units-map (into `{~base-unit 1}                         ;; #: Create the units map
                        (map vec (partition 2 conversions)))]
    `(defmacro ~(symbol (str "unit-of-" name))                  ;; #: Define the unit-of macro
       [~magnitude ~unit]
       `(* ~~magnitude                                          ;; #: Multiply magnitude by target unit
           ~(case ~unit
              ~@(mapcat                                     ;; #: Unroll the unit conversions into a case look-up
                 (fn [[u# & r#]]
                   `[~u# ~(relative-units units-map u#)])
                 units-map))))))

(defunits-of distance :m
  :km 1000
  :cm 1/100
  :mm [1/10 :cm]
  :ft 0.3048
  :mile [5280 :ft])


(comment

  (unit-of-distance 1 :m)
  ;;=> 1

  (unit-of-distance 1 :mm)
  ;;=> 1/1000

  (unit-of-distance 1 :ft)
  ;;=> 0.3048

  (unit-of-distance 1 :mile) 
  ;;=> 1609.344
  
)


(comment

  (def simple-metric {:meter 1, :km 1000, :cm 1/100, :mm [1/10 :cm]})

  (relative-units simple-metric :cm)
  ;;=> 1/100

  (relative-units simple-metric :mm)
  ;;=> 1/1000

  (relative-units {:m 1, :cm 1/100, :mm [1/10 :cm]} :ramsden-chain)
  ;; Runtime....
)



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

  ;;=> 3010.81

  (convert {:bit 1, :byte 8, :nibble [1/2 :byte]} [32 :nibble])
  ;;=> 128N

  (require '[clojure.edn :as edn])

  (edn/read-string "#uuid \"dae78a90-d491-11e2-8b8b-0800200c9a66\"")
  ;;=> #uuid "dae78a90-d491-11e2-8b8b-0800200c9a66"

  (class #uuid "dae78a90-d491-11e2-8b8b-0800200c9a66")
  ;;=> java.util.UUID

  (edn/read-string "42")
  ;;=> 42

  (edn/read-string "{:a 42, \"b\" 36, [:c] 9}")
  ;;=> {:a 42, "b" 36, [:c] 9}
  
  (edn/read-string "#unit/time [1 :min 30 :sec]")
  ;; java.lang.RuntimeException: No reader function for tag unit/time
  
  (def T {'unit/time #'joy.units/time-reader})
  
  (edn/read-string {:readers T} "#unit/time [1 :min 30 :sec]")
  ;;=> 90

  (edn/read-string {:readers T, :default vector} "#what/the :huh?")
  ;;=> [what/the :huh?]
)
