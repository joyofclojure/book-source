(ns joy.event-sourcing)

(defn valid? [event]
  (boolean (:result event)))


(comment
  (valid? {})

  (valid? {:result 42})
  
)