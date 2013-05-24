(ns joy.patterns.abstract-factory)

(def config
  '{:systems {:pump {:type :feeder, :descr "Feeder system"}
              :sim1 {:type :sim,    :descr "Low-fidelity sim",  :fidelity :low}
              :sim2 {:type :sim,    :descr "High-fidelity sim", :fidelity :high, :threads 2}}})

(defn describe-system [name cfg]
  [(:type cfg) (:fidelity cfg)])

(defmulti construct describe-system)

(defmethod construct :default [name cfg]
  {:name name
   :type (:type cfg)})

(defn construct-subsystems [sys-map]
  (for [[name cfg] sys-map]
    (construct name cfg)))

(comment
  
  (construct-subsystems (:systems config))
  ;;=> ({:name :pump, :type :feeder} {:name :sim1, :type :sim} {:name :sim2, :type :sim})

  (describe-system :pump {:type :feeder, :descr "Feeder system"})
)

(comment
  (defmethod construct [:feeder nil]
    [_ cfg]
    (:descr cfg))
  
  (construct-subsystems (:systems config))
  ;;=> ("Feeder system" {:name :sim1, :type :sim} {:name :sim2, :type :sim})
)


