(ns re-frame-puzzle.views
  (:require
   [re-frame.core :as re-frame]
   [re-frame-puzzle.subs :as subs]

   [re-frame-puzzle.events :as events]
   [re-frame-puzzle.stopwatch :as sw]
   [reagent.core :as reagent]
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

(defn genTDs [tiles size]
  (let [last-idx (dec (* size size))]
    (partition
     size
     (for [tile tiles
           :let [idx (:id tile)
                 idx-next (getIdxNext tiles size idx last-idx)]]
       [:td
        (if (empty? idx-next)
          tile
          (assoc tile
                 :on-click
                 #(re-frame/dispatch [::events/move idx-next])))
        (:text tile)
        ])
     )
    ))

(defn genTRs [tds]
  ;; 入れ子リストの各要素の先頭に :tr を追加し、
  ;; 最終的にそのリストを vec で配列に変換している。
  (map #(vec (conj % :tr)) tds))

;;======================

(defn genDDList []
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
    ))

;;======================

(defn genTitle [size]
  ;; タイトル部変更
  (set!
   (.-title js/document)
   (str (dec (* size size)) " Puzzle")))

;;======================

(defn etc []
  (let [size @(re-frame/subscribe [::subs/size])
        goal @(re-frame/subscribe [::subs/goal])

        tiles @(re-frame/subscribe [::subs/tiles])
        shuffled_cnt @(re-frame/subscribe [::subs/shuffled_cnt])
        cnt @(re-frame/subscribe [::subs/cnt])

        log @(re-frame/subscribe [::subs/log])]

    (if (zero? cnt)
      ;; スタート時
      (do
        ;; タイトル部
        (genTitle size)
        ;; メッセージ
        (js/alert (str "Ready? \n\n"
                       "[ SHUFFLED_CNT : " shuffled_cnt " ]"))

        ;; ストップウォッチ：スタート
        (sw/reset)
        (sw/start)

        ;; 空文字列を返さないと、変な数字が出力される
        "")

      ;; 変更時：クリアしたか？
      (if (= goal tiles)
        ;; ストップウォッチ：ストップ
        (let [tm (sw/pause)]
          ;; メッセージ
          (js/setTimeout
           (fn [] (js/alert (str "Clear !! \n\n"
                                 "[ SHUFFLED_CNT : " shuffled_cnt " ] \n"
                                 "[ STEPS : " cnt " ] \n"
                                 "[ TIME : " tm " ]")))
           250)

          ;; ゲームスコアのログを追加登録
          ;; 「re-frame/reg-event-db ::add-log」を呼び出そうとしたものの、
          ;; そういう用途のためのものではないみたい（イベントハンドラーにひもづける、とか）。

          ;; なので、関数として定義しなおし、それを呼び出すことに。
          (events/fn-add-log log size cnt tm)

          ;; 空文字列を返さないと、変な数字が出力される
          "")
        )
      )
    ))

(defn list-log-item [log]
  [:li (str log)])

;;======================

(defn init-tbl []
  [:div

   ;; ドロップダウンリスト
   [genDDList]

   ;; ローカルストレージへのデータ追加ボタン
   ;; 「re-frame/reg-event-db ::add-log」を利用するための練習用に。

   ;; [:button {:on-click
   ;;           #(re-frame/dispatch [::events/add-log
   ;;                                @(re-frame/subscribe [::subs/size])
   ;;                                @(re-frame/subscribe [::subs/cnt])])}
   ;;  "Add Log"]

   ;; 盤
   [:table
    (into
     [:tbody]
     (genTRs
      (genTDs
       @(re-frame/subscribe [::subs/tiles])
       @(re-frame/subscribe [::subs/size])
       )))]

   ;; ログ表示
   (let [logs (vals @(re-frame/subscribe [::subs/log]))
         size @(re-frame/subscribe [::subs/size])]

     [:ul
      (for [l logs
            :when (= size (:level l))]
        ^{:key (:id l)} [list-log-item l]
        )
      ])

   ;; アラートなど
   (etc)

   ])
