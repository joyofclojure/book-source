(ns joy.locks
  "Examples for locking from section 11.5"
  (:refer-clojure :exclude [aget aset count seq])
  (:use [joy.mutation :only (dothreads!)]))

(defprotocol SafeArray ;; #: SafeArray features a small set of functions
  (aset  [this i f])
  (aget  [this i])
  (count [this])
  (seq   [this]))
    
(defn make-dumb-array [t sz]
  (let [a (make-array t sz)]
    (reify
      SafeArray
      (count [_] (clojure.core/count a))
      (seq   [_] (clojure.core/seq a))
      (aget  [_ i] (clojure.core/aget a i)) ;; #: aget and aset are unguarded
      (aset  [this i f]
        (clojure.core/aset a i (f (aget this i)))))))

(defn pummel [a]
  (dothreads! #(dotimes [i (count a)] (aset a i inc)) :threads 100))
    

(defn make-safe-array [t sz]
  (let [a (make-array t sz)] ;; #: Array creation is the same
    (reify
      SafeArray
      (count [_] (clojure.core/count a))
      (seq [_] (clojure.core/seq a))
      (aget [_ i] ;; #: aget is locked
        (locking a
          (clojure.core/aget a i)))
      (aset [this i f] ;; #: aset is locked
        (locking a
          (clojure.core/aset a i (f (aget this i)))))))) ;; #: aset uses aget


(defn lock-i [target-index num-locks] (mod target-index num-locks))


(import 'java.util.concurrent.locks.ReentrantLock)
    
(defn make-smart-array [t sz]
  (let [a   (make-array t sz) ;; #: The array
        Lsz (/ sz 2)
        L   (into-array (take Lsz ;; #: The locks
                              (repeatedly #(ReentrantLock.))))]
    (reify
      SafeArray
      (count [_] (clojure.core/count a))
      (seq [_] (clojure.core/seq a))
      (aget [_ i]
        (let [lk (clojure.core/aget L (lock-i (inc i) Lsz))]
          (.lock lk) ;; #: Explicit locking
          (try 
            (clojure.core/aget a i)
            (finally (.unlock lk))))) ;; #: Explicit unlocking
      (aset [this i f]
        (let [lk (clojure.core/aget L (lock-i (inc i) Lsz))]
          (.lock lk)
          (try
            (clojure.core/aset a i (f (aget this i))) ;; #: Reentrant locking
            (finally (.unlock lk))))))))


(def S (make-smart-array Integer/TYPE 8))
(def A (make-safe-array Integer/TYPE 8))
(def D (make-dumb-array Integer/TYPE 8))
