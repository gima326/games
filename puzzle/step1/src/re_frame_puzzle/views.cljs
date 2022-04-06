(ns re-frame-puzzle.views
  (:require
   [re-frame.core :as re-frame]
   [re-frame-puzzle.subs :as subs]

   [re-frame-puzzle.events :as events]
   [reagent.core :as reagent]
   ))

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

(defn genTDs [tiles size]
  (let [last-idx (dec (* size size))]
    (partition
     size
     (for [tile tiles
           :let [idx (:id tile)
                 idxes (movableIdxes tiles size idx last-idx)]]
       [:td
        (if (empty? idxes)
          tile
          (assoc tile
                 :on-click
                 #(re-frame/dispatch [::events/move (first idxes)])))
        (:text tile)
        ])
     )
    )
  )

(defn genTRs [tds]
  ;; 入れ子リストの各要素の先頭に :tr を追加し、
  ;; 最終的にそのリストを vec で配列に変換している。
  (map #(vec (conj % :tr)) tds))

;;======================

(defn genDDList []
  (let [val (reagent/atom "")]
    (fn []
      [:div "Level: "
       [:select.input-large
        {:on-change
         #(re-frame/dispatch [::events/change (int (.. % -target -value))])
         }
        [:option {:value 3} "3"]
        [:option {:value 4} "4"]
        [:option {:value 5} "5"]
        [:option {:value 6} "6"]
        ]
       ]
      )))

;;======================

(defn init-tbl []
  [:div

   ;; ドロップダウンリスト
   [genDDList]

   (let [tiles @(re-frame/subscribe [::subs/tiles])
         size @(re-frame/subscribe [::subs/size])]

     ;; タイトル
     (set!
      (.-title js/document)
      (str (dec (* size size)) " Puzzle"))

     ;; 盤
     [:table
      (into
       [:tbody]
       (genTRs
        (genTDs tiles size)))]
     )

   ;; クリアしたか？
   (let [t @(re-frame/subscribe [::subs/tiles])
         g @(re-frame/subscribe [::subs/goal])]

     (if (= g t)
       (do
         (js/setTimeout
          (fn [] (js/alert (str "Clear !!")))
          250)
         ;; 空文字列を返さないと、変な数字が出力される
         ""
         )
       ))
   ]
  )
