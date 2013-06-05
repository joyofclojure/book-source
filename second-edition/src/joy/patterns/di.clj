(ns joy.patterns.di)

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

(defprotocol Sys
  (start! [sys])
  (stop!  [sys]))

(defprotocol Sim
  (handle [sim msg]))

(defn construct-subsystems [sys-map]
  (for [[name cfg] sys-map]
    (let [sys (construct name cfg)]
      (start! sys)
      sys)))

(defrecord LowFiSim [name descr])
(defrecord HiFiSim  [name threads descr])

(defmethod construct [:sim :low]
  [name cfg]
  (->LowFiSim name (:descr cfg)))

(defmethod construct [:sim :high]
  [name cfg]
  (->HiFiSim name (:threads cfg) (:descr cfg)))

(comment

  (extend-type LowFiSim
    Sys
    (start! [this] (println "Started a lofi simulator."))
    (stop!  [this] (println "Stopped a lofi simulator."))

    Sim
    (handle [this msg] (* (:weight msg) 3.14)))

  (extend-type HiFiSim
    Sys
    (start! [this] (println "Started a hifi simulator."))
    (stop!  [this] (println "Stopped a hifi simulator."))

    Sim
    (handle [this msg]
      (Thread/sleep 2000)
      (* (:weight msg) 3.1415926535897932384626M)))

  (construct-subsystems (:systems config))
  ;; java.lang.IllegalArgumentException: No implementation of method: :start! of protocol: #'joy.patterns.di/Sys found for class: clojure.lang.PersistentArrayMap

  (defrecord FakeFeeder []
    Sys
    (start! [this] (println "Started a fake feeder" ))
    (stop!  [this] (println "Stopped a fake feeder")))

  (defmethod construct [:feeder nil]
    [name cfg]
    (->FakeFeeder))
  
  (construct-subsystems (:systems config))
  ;;=> ("Feeder system" #joy.patterns.abstract_factory.LowFiSim{:name :sim1, :descr "Low-fidelity sim"} {:name :sim2, :type :sim
)


(comment


  
  (construct-subsystems (:systems config))
  ;;=> ("Feeder system" #joy.patterns.abstract_factory.LowFiSim{:name :sim1, :descr "Low-fidelity sim"} #joy.patterns.abstract_factory.HiFiSim {:name :sim2, :threads nil, :descr "High-fidelity sim"}

)

