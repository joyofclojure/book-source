(ns joy.sim-test
  (:require [joy.event-sourcing :as es]
            [joy.generators :refer (rand-map)]
            [clojure.set :as sql]))


(def PLAYERS #{{:player "Nick", :ability 32}
               {:player "Matt", :ability 26}
               {:player "Ryan", :ability 19}})

(defn lookup [db name]
  (some #(when (= name (:player %)) %) db))

(comment

  (lookup PLAYERS "Nick")
  
  ;;=> {:ability 32, :player "Nick"}
  
)

(defn update-stats [db event]
  (let [player    (lookup db (:player event))
        less-db   (sql/difference db #{player})]
    (conj less-db (merge player (es/effect player event)))))

(comment

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

)

;; Should I show the error modes flag? At least back ref to agents section

(def agent-for-player
  (memoize
   (fn [player-name]
     (agent []))))

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

(defn simulate [total max players]
  (let [events  (apply interleave
                       (for [player players]
                         (rand-events total max player)))
        results (feed-all (ref players) events)]
    (apply await (map #(agent-for-player (:player %)) players))
    @results))




(comment

  (simulate 2 100 PLAYERS)

  ;;=> #{{:ability 32, :player "Nick", :h 2, :avg 1.0, :ab 2}
  ;;     {:ability 19, :player "Ryan", :h 1, :avg 0.5, :ab 2}
  ;;     {:ability 26, :player "Matt", :h 0, :avg 0.0, :ab 2}}

  ;; Wait a few moments
  
  (simulate 400 100 PLAYERS)

  ;;=> #{{:ability 26, :player "Matt", :h 95, :avg 0.2375, :ab 400}
  ;;     {:ability 32, :player "Nick", :h 138, :avg 0.345, :ab 400}
  ;;     {:ability 19, :player "Ryan", :h 66, :avg 0.165, :ab 400}}


  (es/effect-all {} @(agent-for-player "Nick"))

  ;;=> {:ab 402, :h 140, :avg 0.3482587064676617}

  ;; REHYDRATION!!
) 
