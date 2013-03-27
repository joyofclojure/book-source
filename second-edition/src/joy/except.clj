(ns joy.except)

(defn perform-unclean-act [x y]
  (/ x y))

(try
  (perform-unclean-act 42 0)
  (catch RuntimeException ex
    (println (str "Something went wrong."))))

;; Something went wrong.


(defn perform-cleaner-act [x y]
  (try
    (/ x y)
    (catch ArithmeticException ex
      (throw (ex-info "You attempted an unclean act"
                      {:args [x y]})))))

(try
  (perform-cleaner-act 108 0)
  (catch RuntimeException ex
    (println (str "Received error: "
                  (.getMessage ex)))
    (println (str "More information: "
                  (ex-data ex)))))

;; Received error: You attempted an unclean act
;; More information: {:args [108 0]}
;;=> nil
