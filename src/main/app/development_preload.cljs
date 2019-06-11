(ns app.development-preload
  (:require
    [taoensso.timbre :as log]))

; Add code to this file that should run when the initial application is loaded in development mode.
; shadow-cljs already enables console print and plugs in devtools if they are on the classpath,

(js/console.log "Turning logging to :debug (in app.development-preload)")
(log/set-level! :debug)
