(ns re-frame-puzzle.views
  (:require
   [re-frame.core :as re-frame]
   [re-frame-puzzle.subs :as subs]

   [re-frame-puzzle.events :as events]
   [re-frame-puzzle.stopwatch :as sw]
   [reagent.core :as reagent]
   ))

;;======================

(defn genTDs [tiles size]
  (let [last-idx (dec (* size size))]
    (partition
     size
     (for [tile tiles
           :let [idx (:id tile)
                 idx-next (events/getIdxNext tiles size idx last-idx)]]
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
       #(re-frame/dispatch
         [::events/change (int (.. % -target -value))])

       ;; 選択したレベルを、既存値として画面上でも保持させたい。
       :value @(re-frame/subscribe [::subs/size])
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

(defn etc [tiles size log]

  (let [shuffled_cnt @(re-frame/subscribe [::subs/shuffled_cnt])
        goal @(re-frame/subscribe [::subs/goal])
        cnt @(re-frame/subscribe [::subs/cnt])]

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

          ;;(println "etc: " log)

          ;; ゲームスコアのログを追加登録
          (events/fn-add-log log size cnt tm)

          (js/setTimeout
           (fn []
             (do
               ;; メッセージ
               (js/alert (str "Clear !! \n\n"
                              "[ SHUFFLED_CNT : " shuffled_cnt " ] \n"
                              "[ STEPS : " cnt " ] \n"
                              "[ TIME : " tm " ]"))

               ;; リロード（醜いやりかただが、最新のゲームスコアを反映させるためのもの）
               (js/window.location.reload)
               ))
           250)

          ;; 空文字列を返さないと、変な数字が出力される
          "")
        )
      )
    ))

;;======================

(defn list-log-item [log]
  [:li (str log)])

(defn list-log-item2 [log]
  [:tr [:td (str log)]])

(defn bar [log size]
  (fn []
    [:table
     (into
      [:tbody]
      (for [l log ;;(vals log)
            :when (= (:level l) size)]
        ^{:key (:id l)} (list-log-item2 l)))
     ]
    ))

;;======================

(defn init-tbl []
  (let [size @(re-frame/subscribe [::subs/size])
        tiles @(re-frame/subscribe [::subs/tiles])
        log @(re-frame/subscribe [::subs/log])]

    [:div
     ;; ドロップダウンリスト
     [genDDList]

     ;; 盤
     [:table
      (into
       [:tbody]
       (genTRs
        (genTDs tiles size)))]

     ;; ログ表示
     ;; [:ul
     ;;  (for [l (vals log)
     ;;        :when (= (:level l) size)]
     ;;    ^{:key (:id l)} [list-log-item l])]

     ;;[bar log size]

     [:table
      (into
       [:tbody]
       (for [l log ;;(vals log)
             :when (= (:level l) size)]
         ^{:key (:id l)} (list-log-item2 l)))
      ]

     ;; アラートなど
     (etc tiles size log)
     ;;(etc tiles size)
     ]
))
