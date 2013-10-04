(ns joy.unit-testing
  (:require [joy.futures :as joy]))

(def stubbed-feed-children
  (constantly [{:content [{:tag :title
                           :content ["Stub"]}]}]))

(defn count-feed-entries [url]
  (count (joy/feed-children url)))

(comment
  (count-feed-entries "http://blog.fogus.me/feed/")
  
  ;;=> 5

  (with-redefs [joy/feed-children stubbed-feed-children]
    (count-feed-entries "http://blog.fogus.me/feed/"))

  ;;=> 1

  (with-redefs [joy/feed-children stubbed-feed-children]
    (count-feed-entries "this is not a url"))

  ;;=> 1

  (with-redefs [joy/feed-children stubbed-feed-children]
    (joy/occurrences joy/title "Stub" "a" "b" "c"))

  ;;=> 3
)


(require '[clojure.test :refer (deftest testing is)])

(deftest feed-tests
  (with-redefs [joy/feed-children stubbed-feed-children]
    (testing "Child Counting"
      (is (= 1000 (count-feed-entries "Dummy URL"))))
    (testing "Occurrence Counting"
      (is (= 0 (joy/count-text-task
                      joy/title
                      "ZOMG"
                      "Dummy URL"))))))

(clojure.test/run-tests 'joy.unit-testing)

