(ns joy.fixo
  "The persistent binary tree made from records in section 9.3")

(defrecord TreeNode [val l r]) ;;# Define a record type

(defn xconj [t v] ;;# Add to a tree
  (cond
   (nil? t)       (TreeNode. v nil nil)
   (< v (:val t)) (TreeNode. (:val t) (xconj (:l t) v) (:r t))
   :else          (TreeNode. (:val t) (:l t) (xconj (:r t) v))))

(defn xseq [t] ;;# Convert trees to seqs
  (when t
    (concat (xseq (:l t)) [(:val t)] (xseq (:r t)))))


(defprotocol FIXO
  (fixo-push [fixo value])
  (fixo-pop [fixo])
  (fixo-peek [fixo]))

(extend-type TreeNode
  FIXO
  (fixo-push [node value]
    (xconj node value)))

(extend-type clojure.lang.IPersistentVector
  FIXO
  (fixo-push [vector value]
    (conj vector value)))

(extend-type nil
  FIXO
  (fixo-push [t v]
    (TreeNode. v nil nil)))

(extend-type TreeNode
  FIXO
  (fixo-push [node value] ;;# Delegate to xconj
    (xconj node value))
  (fixo-peek [node] ;;# Walk down left nodes to find smallest
    (if (:l node)
      (recur (:l node))
      (:val node)))
  (fixo-pop [node] ;;# Build new path down left to removed item
    (if (:l node)
      (TreeNode. (:val node) (fixo-pop (:l node)) (:r node))
      (:r node))))

(extend-type clojure.lang.IPersistentVector
  FIXO
  (fixo-push [vector value] ;;# fixo-push is vector's conj
    (conj vector value))
  (fixo-peek [vector] ;;# peek is peek
    (peek vector))
  (fixo-pop [vector] ;;# pop is pop
    (pop vector)))

(defn fixo-into [c1 c2]
  (reduce fixo-push c1 c2))

(defn fixed-fixo
  ([limit] (fixed-fixo limit []))
  ([limit vector]
     (reify FIXO
       (fixo-push [this value]
         (if (< (count vector) limit)
           (fixed-fixo limit (conj vector value))
           this))
       (fixo-peek [_]
         (peek vector))
       (fixo-pop [_]
         (pop vector)))))
