(ns joy.udp
  "The Universal Design Pattern (UDP) example from section 9.2."
  (:refer-clojure :exclude [get]))

(defn beget [o p] (assoc o ::prototype p))

(def put assoc)

(defn get [m k]
  (when m
    (if-let [[_ v] (find m k)]
      v
      (recur (::prototype m) k))))

(def clone (partial beget {}))

