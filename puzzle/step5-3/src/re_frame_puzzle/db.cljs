(ns re-frame-puzzle.db
  (:require [cljs.reader]
            [re-frame.core :as re-frame]))

;;======================

;; db tag
(def DATA-KEY :log)
(def DATA-KEY2 :size)
(def DATA-KEY3 :type)

;; localstore key
(def LS-KEY "puzzle-ls-logs")
(def LS-KEY2 "puzzle-ls-size")
(def LS-KEY3 "puzzle-ls-type")

(def default-db {})

;;======================

(defn log->local-store
  "Puts log into localStorage"
  [log]

  (do
;;    (println "log2:" log)

    ;; sorted-map written as an EDN map
    (js/localStorage.setItem LS-KEY (str log))
    ))

(defn size->local-store
  "Puts log into localStorage"
  [size]

  ;; sorted-map written as an EDN map
  (js/localStorage.setItem LS-KEY2 (str size)))

(defn type->local-store
  "Puts log into localStorage"
  [type]

  ;; sorted-map written as an EDN map
  (js/localStorage.setItem LS-KEY3 (str type)))

(defn foo []
  (into (sorted-map)
        (some->> (.getItem js/localStorage LS-KEY)
                 (cljs.reader/read-string))))

;;======================

(re-frame/reg-cofx
 ::local-store-log
 (fn [cofx _]

;;   (println (str "cofx: " cofx))

   ;; put the localstore logs into the coeffect under :local-store-log
   (assoc cofx :local-store-log
          ;; read in logs from localstore, and process into a sorted map
          (into ;;[] ;;
                (sorted-map)
                (some->> (.getItem js/localStorage LS-KEY)
                         (cljs.reader/read-string)    ;; EDN map -> map
                         )))))

(re-frame/reg-cofx
 ::local-store-size
 (fn [cofx _]

   ;;   (println (str "cofx: " cofx))

   ;; put the localstore logs into the coeffect under :local-store-log
   (assoc cofx :local-store-size
          ;; read in logs from localstore, and process into a sorted map
          (into (sorted-map)
                (some->> (.getItem js/localStorage LS-KEY2)
                         (cljs.reader/read-string)    ;; EDN map -> map
                         )))))

(re-frame/reg-cofx
 ::local-store-type
 (fn [cofx _]

   ;;   (println (str "cofx: " cofx))

   ;; put the localstore logs into the coeffect under :local-store-log
   (assoc cofx :local-store-type
          ;; read in logs from localstore, and process into a sorted map
          (into (sorted-map)
                (some->> (.getItem js/localStorage LS-KEY3)
                         (cljs.reader/read-string)    ;; EDN map -> map
                         )))))

;;======================
