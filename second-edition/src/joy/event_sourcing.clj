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

(comment

  (effect {} {:result :hit})
  ;;=> {:ab 1 :h 1 :avg 1.0}

  (effect {:ab 599 :h 180}
          {:result :out})
  ;;=> {:ab 600 :h 180 :avg 0.3}

)

(defn apply-effect [state event]
  (if (valid? event)
    (effect state event)
     state))

(comment

  (apply-effect {:ab 600 :h 180 :avg 0.3}
                {:result :hit})

  ;;=> {:ab 601, :h 181, :avg 0.3011647254575707}

)


(def effect-all #(reduce apply-effect %1 %2))


