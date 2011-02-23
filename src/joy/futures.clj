(ns joy.futures
  "Examples for futures from section 11.6"
  (:require (clojure [xml :as xml]))
  (:require (clojure [zip :as zip]))
  (:import  (java.util.regex Pattern)))


(defmulti rss-children class)
(defmethod rss-children String [uri-str]
  (-> (xml/parse uri-str)
      zip/xml-zip
      zip/down
      zip/children))
   
(defn count-tweet-text-task [txt feed]
  (let [items (rss-children feed)                     ;; #: Get kids
        re    (Pattern/compile (str "(?i)" txt))] ;; #: Create regex
    (count 
     (mapcat #(re-seq re (first %)) ;; #: Get matches
             (for [item (filter (comp #{:item} :tag) items)] ;; #: Filter non-items
               (-> item :content first :content)))))) ;; #: Get title

(defmacro as-futures [[a args] & body]
  (let [parts          (partition-by #{'=>} body)
        [acts _ [res]] (partition-by #{:as} (first parts))
        [_ _ task]     parts]
    `(let [~res (for [~a ~args] (future ~@acts))]
       ~@task)))


(defn tweet-occurrences [tag & feeds]
  (as-futures [feed feeds]
     (count-tweet-text-task tag feed)
     :as results
    =>
    (reduce (fn [total res] (+ total @res))
            0
            results)))
