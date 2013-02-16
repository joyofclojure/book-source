(ns joy.cat-test
  (:use joy.cat
        clojure.test))

(deftest test-cats
  (is (= (cat "House" " of Leaves") "House of Leaves"))
  (is (= (cat [1 2 3] [4 5 6]) '(1 2 3 4 5 6))))
