(ns joy.cps
  "Demostrations of continuation-passing style")

(defn fac-cps [n k]
  (letfn [(cont [v] (k (* v n)))] ;; #1_fac-cps: Next continuation
    (if (zero? n)                 ;; #2_fac-cps: Accept continuation
      (k 1)                       ;; #3_fac-cps: Return continuation
      (recur (dec n) cont))))

(defn mk-cps [accept? kend kont]
  (fn [n]
    ((fn [n k]
       (let [cont (fn [v] (k ((partial kont v) n)))] ;; #1_mk-cps: Next continuation
         (if (accept? n) ;; #2_mk-cps: Accept continuation
           (k 1)         ;; #3_mk-cps: Return continuation
           (recur (dec n) cont))))
     n kend)))
    
(def fac (mk-cps zero? identity #(* %1 %2))) ;; #4_mk-cps: Factorial fn
    
(def tri (mk-cps zero? dec #(+ %1 %2)))

