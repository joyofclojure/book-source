(ns joy.patterns.mock
  (:require [joy.patterns.abstract-factory :as factory]
            [joy.patterns.di :as di]))

(defrecord MockSim [name])

(extend-type MockSim
  di/Sys
  (start! [this] (println "Started a mock simulator."))
  (stop!  [this] (println "Stopped a mock simulator."))
  
  di/Sim
  (handle [_ _] 42))

