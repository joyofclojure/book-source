(ns joy.agents
  "Examples for Agents from section 10.2"
  (:use [joy.mutation :only [dothreads!]]))

(def joy (agent []))

(comment
  (send joy conj "First edition")
  @joy
  ;=> ["First edition"]
  )

(defn slow-conj [coll item]
  (Thread/sleep 1000)
  (conj coll item))

(comment
    (send joy slow-conj "Second edition")
    ;=> #<Agent@5efefc32: ["First edition"]>
    @joy
    ;=> ["First edition" "Second edition"]
)

(def log-agent (agent 0))

(defn do-log [msg-id message]
  (println msg-id ":" message)
  (inc msg-id))

(defn do-step [channel message]
  (Thread/sleep 1)
  (send-off log-agent do-log (str channel message)))

(defn three-step [channel]
  (do-step channel " ready to begin (step 0)")
  (do-step channel " warming up (step 1)")
  (do-step channel " really getting going now (step 2)")
  (do-step channel " done! (step 3)"))

(defn all-together-now []
  (dothreads! #(three-step "alpha"))
  (dothreads! #(three-step "beta"))
  (dothreads! #(three-step "omega")))

(defn handle-log-error [the-agent the-err]
  (println "An action sent to the log-agent threw " the-err))

