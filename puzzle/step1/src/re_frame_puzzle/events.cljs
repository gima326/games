(ns re-frame-puzzle.events
  (:require
   [re-frame.core :as re-frame]
   [re-frame-puzzle.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-tiles
 (fn [_ _]
   db/default-tiles))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

;;======================

(re-frame/reg-event-db
 ::move
 (fn [db [_ [from to]]]
   (let [tiles (:tiles db)
         f (tiles from)
         t (tiles to)]

     (update db
             :tiles
             #(assoc tiles
                     to (assoc f :id to)
                     from  (assoc t :id from)))
     )))

(re-frame/reg-event-db
 ::change
 (fn [db [_ val]]
   (let [tiles (vec (db/initTILES val))
         shuffled (db/my-shuffle (atom tiles) val)]

     (-> db
         (update :tiles (fn [t] shuffled))
         (update :size (fn [s] val))
         (update :goal (fn [g] tiles)))
     )))
