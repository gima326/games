(ns re-frame-puzzle.events
  (:require
   [re-frame.core :as re-frame]
   [re-frame-puzzle.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

;;======================

(re-frame/reg-event-db
 ::move
 (fn [db [_ [[from to]]]]
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
 (fn [db [_ size]]
   (let [goal (vec (db/initTILES size))
         shuffled (db/my-shuffle goal size)]

     (-> db
         (update :size (fn [s] size))
         (update :goal (fn [g] goal))
         (update :tiles (fn [t] shuffled)))
     )))
