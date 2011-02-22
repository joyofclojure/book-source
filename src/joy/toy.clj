(ns joy.toy
  "Source code for the persistent binary tree from chapter 6.")

(defn xseq [t]
  (when t
    (concat (xseq (:L t))
            [(:val t)]
            (xseq (:R t)))))

(defn xconj [t v]
  (cond
   (nil? t)       {:val v, :L nil, :R nil}
   (< v (:val t)) {:val (:val t),
                   :L (xconj (:L t) v),
                   :R (:R t)}
   :else          {:val (:val t),
                   :L (:L t),
                   :R (xconj (:R t) v)}))

