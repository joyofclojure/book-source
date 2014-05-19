(ns joy.promises
  "Examples for promises from section 11.7"
  (:use [joy.mutation :only [dothreads!]])
  (:use [joy.futures :only [rss-children]]))

(defmacro with-promises [[n tasks _ as] & body]
  (when as
    `(let [tasks# ~tasks 
           n# (count tasks#)
           promises# (take n# (repeatedly promise))]
       (doseq [i# (range n#)]
         (dothreads!
          (fn []
            (deliver (nth promises# i#)
                     ((nth tasks# i#))))))
       (let [~n tasks#
             ~as promises#]
         ~@body))))

(defrecord TestRun [run passed failed])
    
(defn passr [] true)
(defn failr [] false)
    
(defn run-tests [& all-tests]
  (with-promises 
    [tests all-tests :as results]
    (into (TestRun. 0 0 0) 
          (reduce #(merge-with + %1 %2) {}
                  (for [r results]
                    (if @r
                      {:run 1 :passed 1}
                      {:run 1 :failed 1}))))))


(defn tweet-items [k feed]
  (k 
   (for [item (filter (comp #{:item} :tag) (rss-children feed))]
     (-> item :content first :content))))

(defmacro cps->fn [f k]
  `(fn [& args#]
     (let [p# (promise)]
       (apply ~f (fn [x#] (deliver p# (~k x#))) args#)
       @p#)))
    
(def count-items (cps->fn tweet-items count))

;; philosophers

(def kant (promise)) 
(def hume (promise))

