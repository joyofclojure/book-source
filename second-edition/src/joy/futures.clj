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

(defn content [entry]
  (some->> entry
           :content
           (some #(when (= :content (:tag %)) %))
           :content
           first))

(defn count-text-task [extractor txt feed]
  (let [items (rss-children feed)
        re    (Pattern/compile (str "(?i)" txt))]
    (count 
     (map #(re-seq re (first %))
          (map extractor items)))))

(defmacro as-futures [[a args] & body]
  (let [parts          (partition-by #{'=>} body)
        [acts _ [res]] (partition-by #{:as} (first parts))
        [_ _ task]     parts]
    `(let [~res (for [~a ~args] (future ~@acts))]
       ~@task)))

(defn occurrences [tag & feeds]
  (as-futures [feed feeds]
     (count-text-task tag feed)
     :as results
    =>
    (reduce (fn [total res] (+ total @res))
            0
            results)))

(comment

  (count-text-task
   "Erlang"
   "http://feeds.feedburner.com/ElixirLang")
)