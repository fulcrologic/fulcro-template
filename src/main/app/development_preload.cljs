(ns app.development-preload
  (:require
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro.algorithms.timbre-support :as ts]))

(js/console.log "Turning logging to :debug (in app.development-preload)")
(log/set-level! :debug)
(log/merge-config! {:output-fn ts/prefix-output-fn
                    :appenders {:console (ts/console-appender)}})
