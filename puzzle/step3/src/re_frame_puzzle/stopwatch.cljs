(ns re-frame-puzzle.stopwatch)

(defn seconds-to-time
  [secs]
  (let [d (js/Date. (* secs 1000))]
    {:hours   (.getUTCHours d)
     :minutes (.getUTCMinutes d)
     :seconds (.getUTCSeconds d)}))

(defn pad [n]
  (if (< n 10) (str "0" n) n))

(defn display-time [tm flg]
  (let [hh (pad (:hours tm))
        mm (pad (:minutes tm))
        ss (pad (:seconds tm))
        tm_formatted (str hh ":" mm ":" ss)]
    ;; コンソールログに出力
    (if flg
      (js/console.log tm_formatted))

    tm_formatted))

(let [id (atom 0)
      clock (atom 0)]

  (defn start [& flg]
    (reset! id
            (js/setInterval
             #(do
                (display-time (seconds-to-time @clock) (first flg))
                (swap! clock inc))
             1000)))

  (defn pause []
    (do
      (js/clearInterval @id)
      (display-time (seconds-to-time @clock) false)))

  (defn reset []
    (do
      (js/clearInterval @id)
      (reset! clock 0)))
  )
