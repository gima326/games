(ns re-frame-puzzle.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [re-frame-puzzle.events :as events]
   [re-frame-puzzle.views :as views]
   [re-frame-puzzle.config :as config]
   ))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/init-tbl] root-el)
    ))

(defn init []
  ;; 初期化
  (re-frame/dispatch-sync [::events/initialize-db])

  (dev-setup)

  ;; 描画
  (mount-root))
