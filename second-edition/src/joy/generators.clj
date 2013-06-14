(ns joy.generators)

(def ascii (map char (range 65 (+ 65 26))))

(defn rand-str [sz alphabet]
  (apply str (repeatedly sz #(rand-nth alphabet))))

(def rand-sym #(symbol (rand-str %1 %2)))

(def rand-key #(keyword (rand-str %1 %2)))

(comment

  (rand-str 10 ascii)

  (rand-key 10 ascii)

  (rand-sym 10 ascii)
  
)


