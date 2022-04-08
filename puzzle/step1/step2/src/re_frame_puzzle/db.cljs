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
    ))

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
        last-idx (dec (* size size))
        cnt (atom 0)]

    ;; 「状態」の変更を繰り返していることを示したい。
    (dotimes [i (* 250 size)]
      (let [idx (int (* (rand) size size))
            idx-next (getIdxNext @shuffled size idx last-idx)]

        (if (not (empty? idx-next))
          (do
            (move shuffled idx-next)
            (reset! cnt (inc @cnt))))))

    [@shuffled @cnt]
    ))

;;======================

(def default-db
  (let [size 3
        goal_state (vec (initTILES size))
        [shuffled cnt] (my-shuffle goal_state size)]

    {:size size
     :goal goal_state

     :tiles shuffled
     :shuffled_cnt cnt
     :cnt 0
     }
    ))
