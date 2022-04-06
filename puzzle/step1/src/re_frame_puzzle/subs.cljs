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
