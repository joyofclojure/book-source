(ns joy.chess-test
  (:use joy.chess
        clojure.test))

(deftest test-lookup
  (is (= (lookup (initial-board) "a8") \r)))

