(ns re-frame-puzzle.subs
  (:require
   [re-frame.core :as re-frame]))

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
