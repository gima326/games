(ns re-frame-puzzle.db)

;;======================

(defn initTILES [size]
  (for [idx (range (* size size))
        :let [val (inc idx)
              last? (= val (* size size))]]
    {:id idx
     :value val
     :text (if last? "" val)
     :class (if last? "empty" "tile")}
    )
  )

(def SIZE 3)
(def GOAL_STATE (vec (initTILES SIZE)))

;;======================

(defn getIdxNext [tiles size idx last-idx]
  (for [[idx-next movable?]

        [[(- idx size) (<= 0 (- idx size))]         ;; POS_UP
         [(+ idx size) (<= (+ idx size) last-idx)]  ;; POS_DOWN
         [(dec idx) (< 0 (rem idx size))]           ;; POS_LEFT
         [(inc idx) (< (rem idx size) (dec size))]] ;; POS_RIGHT

        :when (and movable?
                   (<= 0 idx-next)
                   (<= idx-next last-idx)
                   (= (:value (tiles idx-next)) (inc last-idx)))
        ]

    [idx idx-next]
    ))

(defn move [tiles [[from to]]]
  (let [f (@tiles from)
        t (@tiles to)]

    ;; ベクター tiles の要素を入れ換える。
    (reset! tiles
           (assoc @tiles
                   to (assoc f :id to)
                   from (assoc t :id from)))))

(defn my-shuffle [default-tiles size]
  (let [shuffled (atom default-tiles)
        last-idx (dec (* size size))]

    ;; 「状態」の変更を繰り返していることを示したい。
    (dotimes [i (* 250 size)]
      (let [idx (int (* (rand) size size))
            idx-next (getIdxNext @shuffled size idx last-idx)]

        (if (not (empty? idx-next))
          (move shuffled idx-next)
          )))

    @shuffled
    )
  )

;;======================

(def default-db
  {:size SIZE
   :goal GOAL_STATE
   :tiles (my-shuffle GOAL_STATE SIZE)
   })
