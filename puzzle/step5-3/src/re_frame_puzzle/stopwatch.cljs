(ns re-frame-puzzle.stopwatch)

(defn pad [n]
  (if (< n 10) (str "0" n) n))

(defn seconds-to-time [secs]
  (let [d (js/Date. (* secs 1000))
        hh (pad (.getUTCHours d))
        mm (pad (.getUTCMinutes d))
        ss (pad (.getUTCSeconds d))]
    ;; 時間として整形
    (str hh ":" mm ":" ss)))

(let [id (atom 0)
      clock (atom 0)

      cntflg (atom false)]

  (defn start [& flg]
    (if (not @cntflg)
      (do
        ;; フラグ
        (reset! cntflg true)
        ;; タイマー
        (reset! id
                (js/setInterval
                 #(do
                    (if (first flg)
                      ;; コンソールログに出力
                      (js/console.log (seconds-to-time @clock)))
                    (swap! clock inc))
                 1000))

        )
      )
    )

  (defn pause []
    (do
      ;; フラグ
      (reset! cntflg false)

      ;; タイマーの id をクリアする
      (js/clearInterval @id)

      ;; 時間を返す
      (seconds-to-time @clock))
    )

  (defn reset []
    (do
      ;; タイマーの id をクリアする
      (js/clearInterval @id)

      ;; タイマーをリセット
      (reset! clock 0)

      ;; フラグ
      (reset! cntflg false)
      ))
  )
