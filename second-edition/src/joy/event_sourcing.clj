(ns joy.event-sourcing)

(defn valid? [event]
  (boolean (:result event)))


(comment
  (valid? {})

  (valid? {:result 42})
  
)


(defn effect [{:keys [ab h] :or {ab 0, h 0}}
              event]
  (let [ab (inc ab)
        h (if (= :hit (:result event))
            (inc h)
            h)
        avg (double (/ h ab))]
    {:ab ab :h h :avg avg}))