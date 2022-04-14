(ns re-frame-puzzle.events
  (:require
   [re-frame.core :as re-frame]
   [re-frame-puzzle.db :as db]))

;;======================

(re-frame/reg-event-db
 ::move
 (fn [db [_ [[from to]]]]
   (let [tiles (:tiles db)
         f (tiles from)
         t (tiles to)]

     (-> db
         (update :tiles
                 #(assoc tiles
                         to (assoc f :id to)
                         from  (assoc t :id from)))
         (update :cnt #(inc %)))
     )))


(re-frame/reg-event-db
 ::change
 (fn [db [_ size]]
   (let [goal_state (vec (db/initTILES size))
         [shuffled cnt] (db/my-shuffle goal_state size)]

     (-> db
         (update :size (fn [n] size))
         (update :goal (fn [g] goal_state))

         (update :tiles (fn [t] shuffled))
         (update :shuffled_cnt (fn [sc] cnt))
         (update :cnt (fn [c] 0))
         )
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
  (re-frame/inject-cofx ::db/local-store-log)

  ;; after event handler runs, check app-db for correctness.
  check-interceptor
  ]

 ;; 引数名「local-store-log」は、
 ;; 「db/re-frame/::local-store-log」と一致させないと値がとれない。
 (fn [{:keys [db local-store-log]} _]

;;   (println "db: " db)
;;   (println "local-store-log: "  local-store-log)

   (let [db-new {:db (assoc db/default-db db/DATA-KEY local-store-log)}]
;;     (println "db-new: " db-new)
     db-new
     )))

(def puzzle-interceptors
  [
   ;; ensure the spec is still valid  (after)
   check-interceptor

   ;; the 1st param given to handler will be the value from this path within db
   ;; ":log" は、db.cljs、subs.cljs と一致させる必要がある。
   (re-frame/path db/DATA-KEY)

   ;; write log to localstore  (after)
   ->local-store
   ])

;;======================

(defn- allocate-next-id [log]
  "Returns the next log id.
  Assumes log are sorted.
  Returns one more than the current largest id."
  (-> log
      keys
      last
      ((fnil inc 0))))

(defn- getDateTime []
  (let [pad (fn [n] (if (< n 10) (str "0" n) n))
        dt (js/Date.)

        y (.getFullYear dt)
        m (pad (inc (.getMonth dt)))
        d (pad (.getDate dt))

        hh (pad (.getUTCHours dt))
        mm (pad (.getUTCMinutes dt))
        ss (pad (.getUTCSeconds dt))]

    ;; 日時として整形
    (str y "/" m "/"  d " " hh ":" mm ":" ss)
    ))

(re-frame/reg-event-db
 ::add-log

 ;; インターセプター
 puzzle-interceptors

 (fn [log [_ size steps]]
   (let [id
         (allocate-next-id log)

         new-record
         {:id id
          :level size
          :steps steps
          :datetime (getDateTime)
          }

         log-new
         (conj log {id new-record})
         ]

;;     (println "id: " id)
;;     (println "log-new: " log-new)

     log-new))
 )

(defn fn-add-log [log size steps tm]
  (let [id
        (allocate-next-id log)

        new-record
        {:id id
         :level size
         :steps steps
         :time tm
         :datetime (.toLocaleString (js/Date.))

         ;; js/Date そのままだと、日本の時間にならないみたい。
         ;; ごりごり自作するよりフォーマットを諦めて、toLocaleString() を使うことにした。
         ;; (getDateTime)
         }

        log-new
        (conj log {id new-record})
        ]

    (println "log-new: " log-new)

    ;; 更新した log でローカルストレージを上書きする
    (db/log->local-store log-new))
 )
