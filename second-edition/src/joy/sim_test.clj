(ns joy.sim-test
  (:require [joy.event-sourcing :as es]
            [joy.generators :refer (rand-map)]
            [clojure.set :as sql]))


(def PLAYERS #{{:player "Nick", :ability 32}
               {:player "Matt", :ability 26}
               {:player "Ryan", :ability 19}})

(defn update-stats [db event]
  (let [player (first (sql/select #(= (:player event)
                                      (:player %))
                                  db))
        less-db  (sql/difference db #{player})]
    (conj less-db (merge player (es/effect player event)))))

(comment

  (sql/select #(= "Nick" (:player %)) PLAYERS)

  ;;=> #{{:ability 32, :player "Nick"}}

  (update-stats PLAYERS {:player "Nick", :result :hit})

  ;;=> #{{:ability 19, :player "Ryan"}
  ;;     {:ability 32, :player "Nick", :h 1, :avg 1.0, :ab 1}
  ;;     {:ability 26, :player "Matt"}}

)

(defn rand-event [max player]
  (rand-map 1
            #(-> :result)
            #(if (< (rand-int max) (:ability player))
               :hit
               :out)))

(defn rand-events [total max player]
  (take total
        (repeatedly #(assoc (rand-event max player) :player (:player player)))))

(comment

  (rand-events 3 100 {:player "Nick", :ability 32})

  ;;=> ({:player "Nick", :result :out} {:player "Nick", :result :hit} {:player "Nick", :result :out})
  
  
  (reduce
   #(+ %1 (if (= :hit (:result %2)) 1 0))
   0
   (take 100 (repeatedly #(rand-event 100 {:player "Nick", :ability 32}))))

)

(def agent-for-player
  (memoize
   (fn [player-name]
     (let [a (agent [])]
       (set-error-handler! a #(println "bad" %1 %2))
       (set-error-mode! a :fail)
       a))))

(defn feed [db event]
  (let [a (agent-for-player (:player event))]
    (println "Send to" a)
    (send a
          (fn [state]
            (dosync (alter db update-stats event))
            (conj state event)))))

(defn feed-all [db events]
  (doseq [event events]
    (feed db event))
  db)


(comment

  (let [db (ref PLAYERS)]
    (feed-all db (rand-events 100 100 {:player "Nick", :ability 32}))
;;    (await (agent-for-player "Nick"))  ;; NOTE not here, you might see diff count below
    db)

  ;;=> #<Ref@321881a2: #{{:ability 19, :player "Ryan"}
  ;;                     {:ability 26, :player "Matt"}
  ;;                     {:ability 32, :player "Nick", :h 27, :avg 0.27, :ab 100}}

  (count @(agent-for-player "Nick"))

  ;;=> 100

  (es/effect-all {} @(agent-for-player "Nick"))
  ;;=> {:ab 100, :h 27, :avg 0.27}

)

(defn simulate [db player events]
  (let []))

