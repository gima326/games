(ns re-frame-puzzle.svg)

;; user=> (gen-tag "circle" '(r 150 cx 150 cy 150 fill "rgb(155,0,0)"))
;; [:circle {:r 150, :fill "rgb(155,0,0)", :cx 150, :cy 150}]

;; user=> (gen-tag "svg" '(xmlns "aaa" style (width "1" height "2")))
;; [:svg {:xmlns "aaa", :style {:width "1", :height "2"}}]

;;================
;; 補助関数
;;================

(defn gen-attrs [attrs]
  (->>
   (loop [[car & cdr] attrs rslt ()]
     (if (nil? car)
       ;; 終端に達した
       rslt
       (recur (rest cdr)
              (concat rslt
                      (list
                       ;; キーワード（属性名）に変換
                       (keyword car)
                       ;; 値
                       (if (seq? (first cdr))
                         ;; 入れ子リストの場合
                         (gen-attrs (first cdr))
                         ;; 通常の値の場合
                         (first cdr)))))))

   (apply hash-map)))

(defn gen-tag [name alist & child]
  (vec
   (concat
    ;; 親タグ
    (if (empty? alist)
      (list (keyword name))
      (list (keyword name) (gen-attrs alist)))

    ;; 子タグ
    child
    )
   ))

(defn gen-line [[x1 y1 x2 y2] & alist]
  (gen-tag "line"
           (concat (list
                    'x1 x1
                    'y1 y1
                    'x2 x2
                    'y2 y2) alist)))

(defn gen-text [str-disp [x y] & alist]
  (gen-tag "text"
           (concat (list
                    'x x
                    'y y) alist)
           str-disp))

;;================

(let [SIZE 6

      MIN_X (+ (* SIZE 2) 10)
      MIN_Y 10
      MAX_X (+ (* SIZE 82) 10)
      MAX_Y (+ 10 (* SIZE 35))

      SPACE-SIZE 20

      T_STEPS 1
      T_TIME 2
      T_BOTH 3]

  (defn gen-graph [type size logs]

    (gen-tag
     "svg"
     '(xmlns "http://www.w3.org/2000/svg" style (width "600px" height "250px"))

     ;;===================

     ;; 枠線：縦
     (gen-line [MIN_X MIN_Y MIN_X MAX_Y] 'stroke "gray") ;; 左側
     (gen-line [MAX_X MIN_Y MAX_X MAX_Y] 'stroke "gray") ;; 右側

     ;; 枠線：横
     (gen-line [MIN_X MAX_Y MAX_X MAX_Y] 'stroke "gray")

     ;; 枠線：横（点線）
     (for [i (range 3)]
       (gen-line
        [MIN_X (+ 40 (* SIZE 10 i))
         MAX_X (+ 40 (* SIZE 10 i))]

        ;; シーケンス内で、各 line を判別させるために必要
        'key i
        'stroke "red"
        'stroke-dasharray "5, 2"))

     ;;===================

     ;; 下部ラベル
     (let [cnt (atom -1)]
       (for [l logs
             :let [id (:id l)]]
         (do
           (swap! cnt inc @cnt)
           (gen-text
            ;; 表示用文字列
            id

            ;; ポイント：[x y]
            [(+ MIN_X 20 (* @cnt 40)) (+ 225 10)]

            ;; シーケンス内で、各 text を判別させるために必要
            'key id
            'text-anchor "middle"))))

     ;;===================

     ;; 目盛りラベル（30 はじまりの、25 刻み）
     (if (contains? #{T_STEPS T_BOTH} type)
       ;; 右部
       (for [i (range 3)]
         (gen-text
          ;; 目盛りの文字列
          (* (* 25 (- size 2)) (- 3 i))

          ;; ポイント：[x y]
          [(+ MAX_X 10) (+ 45 (* 10 SIZE i))]

          ;; シーケンス内で、各 text を判別させるために必要
          'key i
          'stroke "green")))

     (if (contains? #{T_TIME T_BOTH} type)
       ;; 左部
       (for [i (range 3)]
         (gen-text
          ;; 目盛りの文字列
          (* (- size 2) (- 3 i))

          ;; ポイント：[x y]
          [5 (+ 45 (* 10 SIZE i))]

          ;; シーケンス内で、各 text を判別させるために必要
          'key i
          'stroke "blue")))

     ;;===================

     ;; 帯グラフ（25 刻み、2.4 ポイント / step）
     (if (contains? #{T_STEPS T_BOTH} type)

       (let [cnt (atom -1)]
         (for [l logs]
           (do
            (swap! cnt inc @cnt)
            (gen-tag
             "rect"
             (list
              ;; シーケンス内で、各 rect を判別させるために必要
              'key (:id l)

              'x (+ MIN_X 10 (* @cnt 40))
              'y (- MAX_Y (/ (* (:steps l) 2.4) (- size 2)))
              'width 20
              'height (+ (/ (* (:steps l) 2.4) (- size 2)))
              'rx 0
              'ry 0
              'fill "green"
              'stroke "none"))))))

     ;;===================

     ;; 折れ線グラフ
     (if (contains? #{T_TIME T_BOTH} type)

       (let [times (vec (map #(:time %) logs))

             xy_list (for [i (range (count times))
                           :let [time-vec (clojure.string.split (times i) #":")
                                 seconds (+ (* (int (time-vec 0)) 60 60) ;; h
                                            (* (int (time-vec 1)) 60)    ;; m
                                            (int (time-vec 2)))]]        ;; s

                       [(+ SPACE-SIZE MIN_X (* i (* SPACE-SIZE 2))) ;; x
                        (- MAX_Y (/ seconds (- size 2)))])          ;; y

             [m l] (split-at 4
                             (apply concat
                                    (for [[x y] xy_list] [x " " y " "])))

             points (apply str (concat "M " m "L " l))]

         ;; 折れ線と水玉をあわせて、グラフにする
         (concat
          ;; 折れ線
          (list
           (gen-tag "path" (list
                            'd points

                            ;; シーケンス内で、circle と判別させるために必要
                            'key (str points)
                            'fill "none"
                            'stroke "blue")))

          ;; 水玉
          (for [[x y] xy_list]
            (gen-tag "circle" (list
                               'cx x
                               'cy y
                               'r 1.5

                               ;; シーケンス内で、各 circle を判別させるために必要
                               'key (str x " " y)
                               'stroke "blue")))))
       )

     ;;===================

     )
    )
  )
