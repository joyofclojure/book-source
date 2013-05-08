(ns joy.futures
  "Examples for futures from section 11.6"
  (:require (clojure [xml :as xml]))
  (:require (clojure [zip :as zip]))
  (:import  (java.util.regex Pattern)))


(defmulti rss-children class)

(defmethod rss-children String [uri-str]
  (->> (xml/parse uri-str)
       zip/xml-zip
       zip/children
       (filter (comp #{:item :entry} :tag))))

(defn title [entry]
  (some->> entry
           :content
           (some #(when (= :title (:tag %)) %))
           :content
           first))

(defn count-tweet-text-task [txt feed]
  (let [items (rss-children feed)                     ;; #: Get kids
        re    (Pattern/compile (str "(?i)" txt))] ;; #: Create regex
    (count 
     (mapcat #(re-seq re (first %)) ;; #: Get matches
             (for [item items] ;; #: Filter non-items
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

(comment

  (count-tweet-text-task
   "#clojure"
   "http://twitter.com/statuses/user_timeline/46130870.rss")

  (count-tweet-text-task
   "Erlang"
   "http://feeds.feedburner.com/ElixirLang")

  (def e (rss-children "http://feeds.feedburner.com/ElixirLang"))

  (map title e)

  
  
  (map title e)
  (-> (first e) :content first :tag)
  (-> (first e) :content (nth 5) :tag)
  
)