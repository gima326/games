(ns re-frame-puzzle.subs
  (:require
   [re-frame.core :as re-frame]
   [re-frame-puzzle.db :as db]))

(re-frame/reg-sub
 ::tiles
 (fn [db]
   (:tiles db)))

(re-frame/reg-sub
 ::size
 (fn [db]
   (:size db)))

(re-frame/reg-sub
 ::goal
 (fn [db]
   (:goal db)))

(re-frame/reg-sub
 ::cnt
 (fn [db]
   (:cnt db)))

(re-frame/reg-sub
 ::shuffled_cnt
 (fn [db]
   (:shuffled_cnt db)))

(re-frame/reg-sub
 ::log
 (fn [db]
   (:log db)))

;;=======================

(re-frame/reg-sub
 ::rslt
 (fn [db]
   (do

     (println "msg: " db)

     ;; シンプルに、db/DATA-KEY で目的の値を取得したいが、
     ;; フレームワークのクセなのか、同じ db/DATA-KEY に紐付けられた値としてラップされる。

     (db/DATA-KEY db)
;;     (db/DATA-KEY (db/DATA-KEY db))
     )))
