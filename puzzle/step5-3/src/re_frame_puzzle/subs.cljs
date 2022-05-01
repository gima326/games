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

(re-frame/reg-sub
 ::type
 (fn [db]
   (:type db)))

(re-frame/reg-sub
 ::event
 (fn [db]
   (:event db)))

(re-frame/reg-sub
 ::status
 (fn [db]
   (:status db)))
