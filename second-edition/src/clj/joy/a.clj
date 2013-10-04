(ns joy.a
  "Source for the A* implementation in section 7.4")

(def world [[  1   1   1   1   1]
            [999 999 999 999   1]
            [  1   1   1   1   1]
            [  1 999 999 999 999]
            [  1   1   1   1   1]])


(defn neighbors
  ([size yx]
     (neighbors [[-1 0] [1 0] [0 -1] [0 1]] size yx))
  ([deltas size yx]
     (filter (fn [new-yx] (every? #(< -1 % size)
                                  new-yx))
             (map #(vec (map + yx %)) deltas))))

(defn estimate-cost [step-cost-est sz y x]
  (* step-cost-est 
     (- (+ sz sz) y x 2)))
    
(defn path-cost [node-cost cheapest-nbr]
  (+ node-cost
     (or (:cost cheapest-nbr) 0)))

(comment

  (path-cost 900 {:cost 1})
  ;;=> 901
  
)

(defn total-cost [newcost step-cost-est size y x]
  (+ newcost 
     (estimate-cost step-cost-est size y x)))

(defn min-by [f coll]
  (when (seq coll)
    (reduce (fn [min this]
              (if (> (f min) (f this)) this min))
            coll)))

(defn astar [start-yx step-est cell-costs]
  (let [size (count cell-costs)]
    (loop [steps 0
           routes (vec (replicate size (vec (replicate size nil))))
           work-todo (sorted-set [0 start-yx])]
      (if (empty? work-todo)                    ;; #: Check done
        [(peek (peek routes)) :steps steps] ;; #: Grab the first route
        (let [[_ yx :as work-item] (first work-todo) ;; #: Get next work item
              rest-work-todo (disj work-todo work-item) ;; #: Clear from todo
              nbr-yxs (neighbors size yx)    ;; #: Get neighbors
              cheapest-nbr (min-by :cost     ;; #: Calc least-cost
                                   (keep #(get-in routes %) 
                                         nbr-yxs))
              newcost (path-cost (get-in cell-costs yx) ;; #: Calc path so-far
                                 cheapest-nbr)
              oldcost (:cost (get-in routes yx))]
          (if (and oldcost (>= newcost oldcost)) ;; #: Check if new is worse
            (recur (inc steps) routes rest-work-todo)
            (recur (inc steps) ;; #: Place new path in the routes
                   (assoc-in routes yx
                             {:cost newcost 
                              :yxs (conj (:yxs cheapest-nbr []) 
                                         yx)})
                   (into rest-work-todo ;; #: Add the estimated path to the todo and recur
                         (map 
                          (fn [w] 
                            (let [[y x] w]
                              [(total-cost newcost step-est size y x) w]))
                          nbr-yxs)))))))))

