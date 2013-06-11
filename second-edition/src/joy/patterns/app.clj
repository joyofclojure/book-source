(ns joy.patterns.app
  (require [joy.patterns.di :as di]))

(def config {:type :mock
             :lib  'joy.patterns.mock})

(defn initialize [name cfg]
  (let [lib (:lib cfg)]
    (require lib)
    (di/build-system name cfg)))


(comment

  (initialize :mock-sim config)
  ;; Started a mock simulator.
  ;;=> #joy.patterns.mock.MockSim{:name :mock-sim}

  (di/handle (initialize :mock-sim config) {})
  ;;=> 42

  (di/handle (initialize :mock-sim config) {})
  ;; java.lang.RuntimeException: Called start! more than once.  
)