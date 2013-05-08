(ns joy.unit-testing
  (:require [joy.futures :refer (feed-children)]))

(def stubbed-feed-children
  (constantly [{:tag :title :content ["Stub"]}]))

(defn count-feed-entries [url]
  (count (feed-children url)))

(comment
  (count-feed-entries "http://blog.fogus.me/feed/")
  
  ;;=> 5

  (with-redefs [feed-children stubbed-feed-children]
    (count-feed-entries "http://blog.fogus.me/feed/"))

  ;;=> 1
)


