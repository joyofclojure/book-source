(ns joy.sql-test
  (:require [joy.sql :as sql]
            [clojure.test :refer (deftest is testing)]))

(deftest test-sql
  (testing "That the SQL examples from chapter 1 work properly"
    (is (= (sql/query 9)
           ["SELECT a, b, c FROM X LEFT JOIN Y ON (X.a = Y.b) WHERE ((a < 5) AND (b < ?))" [9]]))))

