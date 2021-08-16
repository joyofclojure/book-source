(ns joy.cps
  "Demostrations of continuation-passing style")

(defn fac-cps [n k]
  (letfn [(cont [v] (k (* v n)))] ;; #1_fac-cps: Next
    (if (zero? n)                 ;; #2_fac-cps: Accept
      (k 1)                       ;; #3_fac-cps: Return
      (recur (dec n) cont))))

(defn fac [n]
  (fac-cps n identity))

(assert (= (fac 10) 3628800))

;; defined use loop/recur
(defn mk-cps [accept? kend kont]
  (fn [n]
    (loop [n n, k kend]
       (if (accept? n) ;; accept
         (k 1)         ;; return
         (recur (dec n)
                (fn [v]
                  (k ((partial kont v) n)))))))) ;; next

;; defined using fn instead of loop, as shown in book text
(defn mk-cps [accept? kend kont]
  (fn [n]
    ((fn [n k]
       (let [cont (fn [v]
                    (k ((partial kont v) n)))] ;; #1_mk-cps: Next
         (if (accept? n) ;; #2_mk-cps: Accept
           (k 1)         ;; #3_mk-cps: Return
           (recur (dec n) cont))))
     n kend)))

(def fac               ; Factorial
  (mk-cps zero?        ; ... ends when 0
          identity     ; ... returns 1 at bottom of stack
          #(* %1 %2))) ; ... multiplies up the stack

(assert (= (fac 10) 3628800))

(def tri               ; Triangles
  (mk-cps #(== 1 %)    ; ... ends when 1
          identity     ; ... returns 1 at bottom of stack
          #(+ %1 %2))) ; ... sums up the stack

(assert (= (tri 10) 55))
