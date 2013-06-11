(ns joy.patterns.mock
  (:require [joy.patterns.abstract-factory :as factory]
            [joy.patterns.di :as di]))

(defrecord MockSim [name])

