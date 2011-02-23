(ns joy.atoms
  "Examples for Atoms in section 11.4"
  (:use [joy.mutation :only [dothreads!]]))

(def *time* (atom 0))
(defn tick [] (swap! *time* inc))


(defn manipulable-memoize [function]
  (let [cache (atom {})] ;; #: Store cache in Atom
    (with-meta 
      (fn [& args]
        (or (@cache args) ;; #: Check cache first
            (let [ret (apply function args)]    ;; #: Else calculate
              (swap! cache assoc args ret)      ;; #: Store result
              ret)))                            ;; #: Return result
      {:cache cache})))                         ;; #: Attach metadata

