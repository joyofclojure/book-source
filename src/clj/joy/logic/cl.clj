(ns joy.logic.cl
  (require [clojure.core.logic :as logic]))

(logic/run* [answer]
            (logic/== answer 5))

(logic/run* [_]
            (logic/fresh [answer]
                         (logic/== answer 5)))

(logic/run* [q]
      (logic/fresh [a]
             (logic/membero a [1 2 3])
             (logic/membero q [3 4 5])
             (logic/== a q)))

(logic/run* [h]
            (logic/fresh [t]
                         (logic/== [h :tail] [:head :tail])
                         (logic/== [:head t] [:head :tail])))

(logic/run* [val1 val2]
  (logic/== {:a val1 :b 2} {:a 1 :b val2}))

    (logic/run* [val1]
	  (logic/fresh [val2]
        (logic/== {:a val1 :b 2} 
                  {:a 1    :b val2})))

    (logic/run* [q]
	  (logic/fresh [val1 val2]
        (logic/== {:a val1 :b 2} 
                  {:a 1    :b val2})
        (logic/== q [val1 val2])))


(logic/run* [x y]    
  (logic/== x y))

(logic/run* [x y]
  (logic/== x y)
  (logic/== y x))

(logic/run* [q]
  (logic/conde
   [(logic/== q 1)]
   [(logic/== q 2)]))

	(logic/run* [george]
	  (logic/conde
	   [(logic/== george :born)]
	   [(logic/== george :unborn)]))


(logic/run* [h t]
            (logic/conde
             [logic/succeed
              (logic/== [h t] [:head :tail])]

             [logic/fail
              (logic/== 1 2)]))

(logic/run* [answer]
            (logic/fresh [x y]
                         (logic/== x y)
                         (logic/== answer [x y])))

(logic/run* [q]
            (logic/== [[[[q]]]] [[[[5]]]]))

(logic/run* [q]
            (logic/fresh [x y]
                         (logic/== [1 x] [y 5])
                         (logic/== {'x x 'y y} q)))

