(ns re-frame-puzzle.db)

(def SIZE 3)
(def TILES (atom []))
(def GOAL_STATE (atom []))

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

;;======================

(defn movableIdxes [tiles size idx last-idx]
  (for [[idx-next movable?]

        [
         [(- idx size) (<= 0 (- idx size))]               ;; POS_UP
         [(+ idx size) (<= (+ idx size) last-idx)]        ;; POS_DOWN

         ;; 左端、右端のセルでないこと（これ以上、横に移動できない）
         [(dec idx) (not (zero? (rem idx size)))]        ;; POS_LEFT
         [(inc idx) (not (= (rem idx size) (dec size)))] ;; POS_RIGHT
         ]

        :when (and movable?
                   (<= 0 idx-next)
                   (<= idx-next last-idx)
                   (= (:value (tiles idx-next)) (inc last-idx)))
        ]

    [idx idx-next]
    ))

(defn move [tiles [from to]]
  (let [f (@tiles from)
        t (@tiles to)]

    ;; ベクターの要素を入れ換える。
    (reset! tiles
           (assoc @tiles
                   to (assoc f :id to)
                   from (assoc t :id from)))))

(defn my-shuffle [tiles size]
  (let [shuffle-cnt (* 250 size)
        last-idx (dec (* size size))]

    ;; move の履歴すべてを保持しているが、
    ;; 必要なのは最後の要素のみなので、last を使っている。
    (last
     (for [i (range shuffle-cnt)
           :let [idx (int (* (rand) size size))
                 idxes (movableIdxes @tiles size idx last-idx)]
           :when (not (empty? idxes))]

       (move tiles (first idxes)))
     )))

;;======================

(def default-tiles
  (do
    ;; タイル：初期化
    (reset! TILES (vec (initTILES SIZE)))
    ;; クリア状態
    (reset! GOAL_STATE @TILES)
    ;; タイル：シャッフル
    (my-shuffle TILES SIZE)))

(def default-db
  {:size SIZE
   :goal @GOAL_STATE
   :tiles @TILES
   })
