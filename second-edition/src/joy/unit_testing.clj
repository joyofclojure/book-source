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

  (with-redefs [feed-children stubbed-feed-children]
    (count-feed-entries "http://blog.fogus.me/feed/"))

  ;;=> 1

  (with-redefs [feed-children stubbed-feed-children]
    (count-feed-entries "this is not a url"))

  ;;=> 1

  (with-redefs [feed-children stubbed-feed-children]
    (joy/occurrences joy/title "Stub" "a" "b" "c"))
)


