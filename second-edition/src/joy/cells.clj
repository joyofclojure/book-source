(ns joy.cells)

(defmacro defformula [nm bindings & formula]
  `(let ~bindings
     (let [formula#   (agent ~@formula) ;; #: Create formula as Agent
           update-fn# (fn [key# ref# o# n#] 
                        (send formula# (fn [_#] ~@formula)))]
       (doseq [r# ~(vec (map bindings (range 0 (count bindings) 2)))]
         (add-watch r# :update-formula update-fn#)) ;; #: Add a watch to each reference
       (def ~nm formula#))))
    
(def h (ref 25))
(def ab (ref 100))
    
(defformula avg [at-bats ab hits h] ;; #: Create baseball formula
  (float (/ @hits @at-bats)))

(comment

  @avg
  ;;=> 0.25

  (dosync (ref-set h 33))

  @avg
  ;;=> 0.33

)