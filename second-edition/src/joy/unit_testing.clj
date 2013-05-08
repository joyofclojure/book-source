(ns joy.unit-testing
  (:require [joy.futures :refer (feed-children)]))

(def stubbed-feed-children
  (constantly [{:tag :title :content ["Stub"]}]))