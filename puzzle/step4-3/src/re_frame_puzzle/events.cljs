(ns re-frame-puzzle.events
  (:require
   [re-frame.core :as re-frame]
   [re-frame-puzzle.db :as db]))

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

(defn move [tiles [[from to]]]
  (let [f (@tiles from)
        t (@tiles to)]

    ;; ベクター tiles の要素を入れ換える。
    (reset! tiles
            (assoc @tiles
                   to (assoc f :id to)
                   from (assoc t :id from)))))

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

(re-frame/reg-event-db
 ::move
 (fn [db [_ [[from to]]]]
   (let [tiles (:tiles db)
         f (tiles from)
         t (tiles to)]

     (-> db
         (assoc :tiles
                (assoc tiles
                       to (assoc f :id to)
                       from  (assoc t :id from))
                :cnt
                (inc (:cnt db))))
     )))

(def check-interceptor2
  (re-frame/after

   ;; 無名関数の引き数名を「db」にすると、db の情報をまるごと取得できた。
   (fn [db]
     (let [log  (db/foo)
           db-new (assoc db :log (conj {} log))]

       ;; インターセプターにて、:log を上書きしてみたものの、
       ;; （タイル部分のように）データの変更を即座に反映してくれない…。

       ;; (println "at check-interceptor2")
       ;; (println "db:" db-new)

       db-new))))

(re-frame/reg-event-db
 ::change

;; [check-interceptor2]

 (fn [db [_ size]]
   (let [log (db/foo)
         goal_state (vec (initTILES size))
         [shuffled cnt] (my-shuffle goal_state size)]

     ;;(println "size: " size)
     (println "loglog: " log)
     (println "loglog2: " (= log (:log db)))

     ;; ローカルストレージの :size を上書きしたい
     (db/size->local-store {:size size})

     (-> db
;;         (assoc :log (conj {} log))
         (assoc :size size
                :goal goal_state
                :tiles shuffled
                :shuffled_cnt cnt
                :cnt 0

                ;; ここで、:tiles といっしょに :log を書き換えようとすると、
                ;; ログの表示じたいが行われなくなる。
                ;;:log log
                ))
     )))

;;======================

(def check-interceptor
  (re-frame/after
   ;; after のタイミングで実行する、なんらかの関数を設定しないといけない。
   ;; 引数の有無は問わないみたい。
   (fn [] (println "at check-interceptor"))))

(def ->local-store
  (re-frame/after
   ;; after のタイミングで実行する、なんらかの関数を設定しないといけない。
   ;; 引数の有無は問わないみたい。
   db/log->local-store))

;; -- Interceptor Chain --

(re-frame/reg-event-fx
 ::initialize-db

 ;; the interceptor chain (a vector of 2 interceptors in this case)
 [
  ;; gets log from localstore, and puts value into coeffects arg
  (re-frame/inject-cofx ::db/local-store-size)

  (re-frame/inject-cofx ::db/local-store-log)

  ;; after event handler runs, check app-db for correctness.
  check-interceptor
  ]

 ;; 引数名「local-store-log」は、
 ;; 「db/re-frame/::local-store-log」と一致させないと値がとれない。
 (fn [{:keys [db local-store-log local-store-size]} _]

   ;;(println "db: " db)
   ;;(println "local-store-log: "  local-store-log)
   ;;(println "local-store-size: "  local-store-size)

   (let [ls-size (:size local-store-size)
         ls-log (:log local-store-log)

         ;; 初回起動時にはローカルストレージが未登録なので、ガードを掛けている。
         size (if (nil? ls-size) 3 ls-size)
         log (if (nil? ls-log) {} ls-log)

         ;; 盤面を作成
         goal_state (vec (initTILES size))
         [shuffled cnt] (my-shuffle goal_state size)

         db-new {:db
                 {:size size
                  :goal goal_state

                  :tiles shuffled
                  :shuffled_cnt cnt
                  :cnt 0

                  db/DATA-KEY log}}
         ]

;;     (println "size: " size)
;;     (println "log: " log)
;;     (println "db-new: " db-new)

     db-new
     )))

;;======================

(defn- allocate-next-id [log]
  "Returns the next log id.
  Assumes log are sorted.
  Returns one more than the current largest id."
  (-> log
      keys
      last
      ((fnil inc 0))))

(defn fn-add-log [log size steps tm]
  (let [id (allocate-next-id log)

        new-record {:id id
                    :level size
                    :steps steps
                    :time tm
                    :datetime (.toLocaleString (js/Date.))}

        log-new (conj log {id new-record})]

;;    (println "log:" log)

    ;; 更新した log でローカルストレージを上書きする
    (db/log->local-store　{:log log-new})))
