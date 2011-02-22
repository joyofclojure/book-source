(ns joy.astar
  "Source for the A* implementation")

(defn neighbors
  ([size yx]
     (neighbors [[-1 0] [1 0] [0 -1] [0 1]] size yx))
  ([deltas size yx]
     (filter (fn [new-yx] (every? #(< -1 % size)
                                  new-yx))
             (map #(map + yx %)
                  deltas))))

