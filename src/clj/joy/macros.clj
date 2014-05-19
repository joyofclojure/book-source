(ns joy.macros
  "Examples of macros from chapter 8")

(defn contextual-eval [ctx expr]
  (eval
   `(let [~@(mapcat (fn [[k v]] [k `'~v]) ctx)]
      ~expr)))

(comment

  (contextual-eval '{a 1, b 2} '(+ a b))
  ;;=> 3

  (contextual-eval '{a 1, b 2} '(let [b 1000] (+ a b)))
  ;;=> 1001  

)

(defmacro do-until [& clauses]
  (when clauses
    (list `when (first clauses)
          (if (next clauses)
            (second clauses)
            (throw (IllegalArgumentException.
                    "do-until requires an even number of forms")))
          (cons 'do-until (next (next clauses))))))

(defmacro unless [condition & body]
  `(if (not ~condition) ;; #1_unless: Unquote condition
     (do ~@body)))      ;; #2_unless: Splice body

(comment

  (unless true (println :nope))

  (unless false (println :yep))

)


(defn from-end [s n] ;; #3_unless: Use our unless
  (let [delta (dec (- (count s) n))]
    (unless (neg? delta) ;; #4_unless: Return nil if negative
            (nth s delta))))

(defmacro def-watched [name & value]
  `(do
     (def ~name ~@value)
     (add-watch (var ~name) 
                :re-bind 
                (fn [~'key ~'r old# new#]
                  (println old# " -> " new#)))))

;; Domain DSL

(defmacro domain [name & body]
  `{:tag :domain, 
    :attrs {:name (str '~name)},
    :content [~@body]})

(declare handle-things)
    
(defmacro grouping [name & body]
  `{:tag :grouping, 
    :attrs {:name (str '~name)},
    :content [~@(handle-things body)]})

(declare grok-attrs grok-props)
    
(defn handle-things [things]
  (for [t things]
    {:tag :thing, 
     :attrs (grok-attrs (take-while (comp not vector?) t))
     :content (if-let [c (grok-props (drop-while (comp not vector?) t))]
                       [c]
                       [])}))
    
(defn grok-attrs [attrs]
  (into {:name (str (first attrs))}
        (for [a (rest attrs)]
          (cond 
           (list? a) [:isa (str (second a))]
           (string? a) [:comment a]))))

(defn grok-props [props]
  (when props
    {:tag :properties, :attrs nil,
     :content (apply vector (for [p props]
                              {:tag :property,
                               :attrs {:name (str (first p))},
                               :content nil}))}))

(def d
  (domain man-vs-monster
          (grouping people                  ;; #: Group of people
                    (Human "A stock human") ;; #: One kind of person
       
                    (Man (isa Human) ;; #: Another kind of person
                         "A man, baby"
                         [name]
                         [has-beard?]))
          (grouping monsters    ;; #: Group of monsters
                    (Chupacabra ;; #: One kind of monster
                     "A fierce, yet elusive creature"
                     [eats-goats?]))))

;; anaphora

(defmacro awhen [expr & body]
  `(let [~'it ~expr]  ;; #1_awhen: Define anaphora
     (if ~'it         ;; #2_awhen: Check its truth
       (do ~@body)))) ;; #3_awhen: Inline the body

;; a Lispy design pattern!

(defmacro with-resource [binding close-fn & body]
  `(let ~binding
     (try (do ~@body)
          (finally
           (~close-fn ~(binding 0))))))

