(ns joy.sim-test
  (:require [joy.event-sourcing :as es]
            [joy.generators :refer (rand-map)]
            [clojure.set :as sql]))

(def PLAYERS #{{:player "Nick", :ability 32/100}
               {:player "Matt", :ability 26/100}
               {:player "Ryan", :ability 19/100}})

(defn lookup [db name]
  (first (sql/select
           #(= name (:player %))
           db)))

(comment

  (lookup PLAYERS "Nick")
  
  ;;=> {:ability 8/25, :player "Nick"}
  
)

(defn update-stats [db event]
  (let [player    (lookup db (:player event))
        less-db   (sql/difference db #{player})]
    (conj less-db (merge player (es/effect player event)))))

(comment

  (update-stats PLAYERS {:player "Nick", :result :hit})

  ;;=> #{{:ability 19/100, :player "Ryan"}
  ;;     {:ability 8/25, :player "Nick", :h 1, :avg 1.0, :ab 1}
  ;;     {:ability 13/50, :player "Matt"}}

)

(defn commit-event [db event]
  (dosync (alter db update-stats event)))

(comment
  (commit-event (ref PLAYERS) {:player "Nick", :result :hit})

  ;;=> #&lt;Ref@658ba666: #{...}&gt; 
)

(defn rand-event [{ability :ability}]
  (let [abil (numerator ability)
        max  (denominator ability)]
    (rand-map 1
              #(-> :result)
              #(if (< (rand-int max) abil)
                 :hit
                 :out))))

(defn rand-events [total player]
  (take total
        (repeatedly #(assoc (rand-event player) 
                            :player 
                            (:player player)))))

(comment

  (rand-events 3 {:player "Nick", :ability 32/100})

  ;;=> ({:player "Nick", :result :out} {:player "Nick", :result :hit} {:player "Nick", :result :out})

)

;; Should I show the error modes flag? At least back ref to agents section

(def agent-for-player
  (memoize
   (fn [player-name]
     (-> (agent [])
         (set-error-handler! #(println "ERROR: " %1 %2))
         (set-error-mode! :fail)))))

(defn feed [db event]
  (let [a (agent-for-player (:player event))]
    (send a
          (fn [state]
            (commit-event db event)
            (conj state event)))))

(defn feed-all [db events]
  (doseq [event events]
    (feed db event))
  db)


(comment

  (let [db (ref PLAYERS)]
    (feed-all db (rand-events 100 {:player "Nick", :ability 32/100}))
;;    (await (agent-for-player "Nick"))  ;; NOTE not here, you might see diff count below
    db)

  ;;=> #<Ref@321881a2: #{{:ability 19/100, :player "Ryan"}
  ;;                     {:ability 13/50,  :player "Matt"}
  ;;                     {:ability 8/25,   :player "Nick", :h 27, :avg 0.27, :ab 100}}

  (count @(agent-for-player "Nick"))

  ;;=> 100

  (es/effect-all {} @(agent-for-player "Nick"))
  ;;=> {:ab 100, :h 27, :avg 0.27}

)

(defn simulate [total players]
  (let [events  (apply interleave
                       (for [player players]
                         (rand-events total player)))
        results (feed-all (ref players) events)]
    (apply await (map #(agent-for-player (:player %)) players))
    @results))




(comment

  (simulate 2 PLAYERS)

  ;;=> #{{:ability 8/25,   :player "Nick", :h 2, :avg 1.0, :ab 2}
  ;;     {:ability 19/100, :player "Ryan", :h 1, :avg 0.5, :ab 2}
  ;;     {:ability 13/50 , :player "Matt", :h 0, :avg 0.0, :ab 2}}

  ;; Wait a few moments
  
  (simulate 400 PLAYERS)

  ;;=> #{{:ability 13/50,  :player "Matt", :h 95, :avg 0.2375, :ab 400}
  ;;     {:ability 8/25,   :player "Nick", :h 138, :avg 0.345, :ab 400}
  ;;     {:ability 19/100, :player "Ryan", :h 66, :avg 0.165, :ab 400}}


  (es/effect-all {} @(agent-for-player "Nick"))

  ;;=> {:ab 402, :h 140, :avg 0.3482587064676617}

  ;; REHYDRATION!!
) 
