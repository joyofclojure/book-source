(ns joy.logic.fd
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :as logic]
            [clojure.core.logic.fd :as fd]))

(logic/run* [q]
  (logic/fresh [x y]
    (logic/== q [x y])))

;;=> ([_0 _1])


(logic/run* [q]
  (logic/fresh [x y]
    (logic/== [:pizza "Java"] [x y])
    (logic/== q [x y])))

;;=> ([:pizza "Java"])


(logic/run* [q]
  (logic/fresh [x y]
    (logic/== q [x y])
    (logic/!= y "Java")))

;;=> (([_0 _1] :- (!= (_1 "Java"))))


	(logic/run* [q]
	  (logic/fresh [x y]
	    (logic/== [:pizza "Java"] [x y])
	    (logic/== q [x y])
            (logic/!= y "Java")))

	;;=> ()


	(logic/run* [q]
	  (logic/fresh [x y]
	    (logic/== [:pizza "Scala"] [x y])
	    (logic/== q [x y])
        (logic/!= y "Java")))



(logic/run* [q]
  (logic/fresh [x y]
    (logic/!= [1 x] [y 2])
    (logic/== q [x y])))

;;=> (([_0 _1] :- (!= (_0 2) (_1 1))))


(logic/run* [q]
  (logic/fresh [n]
    (logic/== q n)))


(logic/run* [q]
  (logic/fresh [n]
    (logic/!= 0 n)                     
    (logic/== q n)))


(logic/run* [q]
  (logic/fresh [n]
    (fd/in n (fd/interval 1 Integer/MAX_VALUE))
    (logic/== q n)))


(logic/run* [q]
  (logic/fresh [n]
    (fd/in n (fd/domain 0 1))
    (logic/== q n)))


;; coin toss
(logic/run* [q]
  (let [coin (fd/domain 0 1)]
    (logic/fresh [heads tails]
      (fd/in heads 0 coin)
      (fd/in tails 1 coin)
      (logic/== q [heads tails]))))
