(ns joy.logic.planets
  (require [clojure.core.logic :as logic]))

(logic/defrel orbits orbital body)

(logic/fact orbits :mercury :sun)
(logic/fact orbits :venus   :sun)
(logic/fact orbits :earth   :sun)
(logic/fact orbits :mars    :sun)
(logic/fact orbits :jupiter :sun)
(logic/fact orbits :saturn  :sun)
(logic/fact orbits :uranus  :sun)
(logic/fact orbits :neptune :sun)

(logic/run* [q]
  (logic/fresh [orbital body]
    (orbits orbital body)
    (logic/== q orbital)))

;;=> (:earth :saturn :jupiter :mars :mercury :neptune :uranus :venus)


;; stars

(logic/defrel stars star)

(logic/fact stars :sun)

(defn planeto [body]
  (logic/fresh [star]
    (stars star)
    (orbits body star)))

(logic/run* [q]
  (planeto :earth))

;;=> (_0)

(logic/run* [q]
  (planeto :earth)
  (logic/== q true))

;;=> (true)

(logic/run* [q]
  (planeto :sun))

;;=> ()

(logic/run* [q]
  (logic/fresh [orbital]
    (planeto orbital)
    (logic/== q orbital)))



(logic/fact stars :alpha-centauri)
(logic/fact orbits :Bb :alpha-centauri)

(logic/run* [q]
  (planeto :Bb))

(logic/run* [q]
  (logic/fresh [orbital]
    (planeto orbital)
    (logic/== q orbital)))

;; satellites

(defn satelliteo [body]
  (logic/fresh [p]
    (orbits body p)
    (planeto p)))

(logic/run* [q]
  (satelliteo :sun))

(logic/run* [q]
  (satelliteo :earth))

(logic/fact orbits :moon :earth)

(logic/run* [q]
  (satelliteo :moon))


;; more data

(logic/fact orbits :phobos :mars)
(logic/fact orbits :deimos :mars)
(logic/fact orbits :io :jupiter)
(logic/fact orbits :europa :jupiter)
(logic/fact orbits :ganymede :jupiter)
(logic/fact orbits :callisto :jupiter)

(logic/run* [q]
  (satelliteo :io))

(logic/run* [q]
  (satelliteo :leda))


