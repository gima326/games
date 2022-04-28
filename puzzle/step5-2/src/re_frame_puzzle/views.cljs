(ns re-frame-puzzle.views
  (:require
   [re-frame.core :as re-frame]
   [re-frame-puzzle.subs :as subs]

   [re-frame-puzzle.events :as events]
   [re-frame-puzzle.stopwatch :as sw]
   [re-frame-puzzle.svg :as svg]
   [reagent.core :as reagent]))

;;======================

(defn addClickEventToTile [tile idx-next]
  [:td
   (if (empty? idx-next)
     tile
     (assoc tile
            :on-click
            #(re-frame/dispatch [::events/move idx-next])))

   ;; タイル（クリックイベント追加済み）
   (:text tile)
   ]
  )

(defn genTDs [tiles size]
  (let [last-idx (dec (* size size))]
    (partition
     size
     (for [t tiles
           :let [idx (:id t)
                 idx-next (events/getIdxNext tiles size idx last-idx)]]

       ;; なくても文句を言われない
       ;;　^{:key idx}

       (addClickEventToTile t idx-next))
     )
    ))

(defn genTRs [tds]
  ;; 入れ子リストの各要素の先頭に :tr を追加し、
  ;; 最終的にそのリストを vec で配列に変換している。
  (map #(vec (conj % :tr)) tds)
  )

;;======================

(defn genLevelDDList []
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

(let [T_NONE 0
      T_STEPS 1
      T_TIME 2
      T_BOTH 3]

  (defn genLogTypeDDList []
    (fn []
      [:div "Log: "
       [:select.input-large
        {:on-change
         #(re-frame/dispatch
           [::events/showlog (int (.. % -target -value))])


         ;; 選択したログ出力タイプを、既存値として画面上でも保持させたい。
         :value @(re-frame/subscribe [::subs/type])
         }

        [:option {:value T_NONE} "-"]
        [:option {:value T_STEPS} "steps"]
        [:option {:value T_TIME} "time"]
        [:option {:value T_BOTH} "both"]
      ]
     ]
    )))

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
        cnt @(re-frame/subscribe [::subs/cnt])
        event @(re-frame/subscribe [::subs/event])]

    (if (zero? cnt)
      ;; スタート時
      (if (not (= "showlog" event))
        (do
          ;; タイトル部
          (genTitle size)
          ;; メッセージ
          (js/alert (str "Ready? \n\n"
                         "[ SHUFFLED_CNT : " shuffled_cnt " ]"))

          ;; ストップウォッチ：リセット
          (sw/reset)

          ;; 空文字列を返さないと、変な数字が出力される
          ""))

      (if (= 1 cnt)
        (do
          ;; ストップウォッチ：スタート
          (sw/start)

          ;; 空文字列を返さないと、変な数字が出力される
          "")))

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
  )

;;======================

(defn list-log-item [log]
  [:li (str log)])

(defn list-log-item2 [log]
  [:tr [:td (str log)]])

(defn list-log-item3 [log type]
  [:tr
   [:td
    (str
     (if (= 1 type)
       (:steps log)
       (if (= 2 type)
         (:time log)
         log)))]])

;;======================

(defn init-tbl []
  ( let [size @(re-frame/subscribe [::subs/size])
         tiles @(re-frame/subscribe [::subs/tiles])
         logs @(re-frame/subscribe [::subs/log])
         type @(re-frame/subscribe [::subs/type])]

    [:div

     [:table
      [:tbody

       ;; １行目
       [:tr

        ;; ゲーム盤面
        [:td
         [:table
          (into
           [:tbody]
           (genTRs
            (genTDs tiles size)))]]

        [:td
         (if (< 0 type)
           (svg/gen-graph
            type
            size
            (for [l logs
                  :when (= (:level l) size)]

              ;; なくても文句を言われない
              ;; ^{:key (str "graph_" (:id l))}

              l)))]
        ]

       ;; ２行目

       [:tr

        ;; ドロップダウンリスト
        [:td
         [:table
          (into
           [:tbody
            [:tr
             [:td [genLevelDDList]]
             [:td [genLogTypeDDList]]]])]]
        ]

       ;; ３行目

       [:tr
        [:td]

        ;; ログ
        [:td
         (if (< 0 type)
           [:table
            (into
             [:tbody]
             (for [l logs
                   :when (= (:level l) size)]

               ;; なくても文句を言われない
               ;; ^{:key (:id l)}

               (list-log-item2 l)))
            ])]
        ]
       ]
      ]

     ;; アラートなど
     (etc tiles size logs)
     ]
))
