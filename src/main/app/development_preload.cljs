(ns app.development-preload
  (:require
    [com.fulcrologic.fulcro.algorithms.timbre-support :as ts]
    [taoensso.timbre :as log]))

(js/console.log "Turning logging to :debug (in app.development-preload)")
(log/set-min-level! :debug)
(log/merge-config! {:output-fn ts/prefix-output-fn
                    :appenders {:console (ts/console-appender)}})
