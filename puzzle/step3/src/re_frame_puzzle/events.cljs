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
