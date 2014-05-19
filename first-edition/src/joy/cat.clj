(ns joy.cat
  "Ploymorphism and expression problem example from chapter 1.")

(defprotocol Concatenatable
  (cat [this other]))
    
(extend-type String
  Concatenatable
  (cat [this other]
    (.concat this other)))

(extend-type java.util.List
  Concatenatable
  (cat [this other]
    (concat this other)))


(comment    
  (cat "House" " of Leaves")
  ;=> "House of Leaves"


  (cat [1 2 3] [4 5 6])
  ;=> (1 2 3 4 5 6)
)