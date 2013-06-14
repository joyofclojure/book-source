(ns joy.sim-test
  (:require [joy.event-sourcing :as es]
            [joy.generators :refer (rand-map)]
            [clojure.set :as set]))


(def db (ref #{{:player "Nick", :ability 32}
               {:player "Matt", :ability 26}
               {:player "Ryan", :ability 19}}))




(defn rand-event [max ability]
  (rand-map 1
            #(-> :result)
            #(if (< (rand-int max) ability)
               :hit
               :out)))

(comment

  (rand-event )

)
