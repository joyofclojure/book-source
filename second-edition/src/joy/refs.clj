(ns joy.refs
  "Examples for Ref in section 10.2"
  (use [joy.mutation :only (dothreads!)]
       [joy.a :only (neighbors)]))

(def initial-board
  [[:- :k :-]
   [:- :- :-]
   [:- :K :-]])

(defn board-map [f bd]
  (vec (map #(vec (for [s %] (f s))) bd)))

(defn reset-board!
  "Resets the board state. Generally these types of functions are a
   bad idea, but matters of page count force our hand."
  []
  (def board (board-map ref initial-board))
  (def to-move (ref [[:K [2 1]] [:k [0 1]]]))
  (def num-moves (ref 0)))

(def king-moves
  (partial neighbors
           [[-1 -1] [-1 0] [-1 1] [0 -1] [0 1] [1 -1] [1 0] [1 1]] 3))

(defn good-move?
  "Returns 'to' position if it isn't already the 'enemy-sq' position."
  [to enemy-sq]
  (when (not= to enemy-sq)
    to))

(defn choose-move
  "Randomly choose a legal move"
  [[[mover mpos][_ enemy-pos]]]
  [mover (some #(good-move? % enemy-pos)
               (shuffle (king-moves mpos)))])

(comment
  (reset-board!)
  (take 5 (repeatedly #(choose-move @to-move)))
  ;=> ([:K [1 0]] [:K [1 2]] [:K [1 1]] [:K [1 2]] [:K [1 0]])
  )

(defn place [from to] to)

(defn move-piece [[piece dest] [[_ src] _]]
  (alter (get-in board dest) place piece)
  (alter (get-in board src ) place :-)
  (alter num-moves inc))

(defn update-to-move [move]
  (alter to-move #(vector (second %) move)))

(defn make-move []  ;; bad!
  (let [move (choose-move @to-move)]
    (dosync (move-piece move @to-move))
    (dosync (update-to-move move))))

(comment
  (reset-board!)

  (make-move)
  ;=> [[:k [0 1]] [:K [2 0]]]
  (board-map deref board)
  ;=> [[:- :k :-] [:- :- :-] [:K :- :-]]

  (make-move)
  ;=> [[:K [2 0]] [:k [1 1]]]
  (board-map deref board)
  ;=> [[:- :- :-] [:- :k :-] [:K :- :-]]

  (dothreads! make-move :threads 100 :times 100)
  (board-map deref board)
  ;=> [[:- :- :-] [:- :K :-] [:- :- :K]]</programlisting></informalexample>  ;; oops!
  )

(defn make-move-v2 [] ;; good!
  (dosync
   (let [move (choose-move @to-move)]
     (move-piece move @to-move)
     (update-to-move move))))

(comment
    (reset-board!)
    (make-move)
    ;=> [[:k [0 1]] [:K [2 0]]]
    (board-map deref board)
    ;=> [[:- :k :-] [:- :- :-] [:K :- :-]]
    @num-moves
    ;=> 1

    (dothreads! make-move-v2 :threads 100 :times 100)
    (board-map #(dosync (deref %)) board)
    ;=> [[:k :- :-] [:- :- :-] [:K :- :-]]
    @to-move
    ;=> [[:k [0 0]] [:K [2 0]]]
    @num-moves
    ;=> 10001
    )


(defn move-piece [[piece dest] [[_ src] _]]
  (commute (get-in board dest) place piece)
  (commute (get-in board src ) place :-)
  (commute num-moves inc))
