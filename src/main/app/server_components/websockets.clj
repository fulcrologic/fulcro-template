(ns app.server-components.websockets
  (:require [mount.core :refer [defstate]]
            [com.fulcrologic.fulcro.networking.websockets :as fws]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
            [app.server-components.pathom :refer [parser]]))


(defstate websockets
  :start
  (fws/start! (fws/make-websockets
                parser
                {:http-server-adapter (get-sch-adapter)
                 :parser-accepts-env? true
                 ;; See Sente for CSRF instructions
                 :sente-options       {:csrf-token-fn nil}}))
  :stop
  (fws/stop! websockets))

(defn wrap-websockets [handler]
  (fws/wrap-api handler websockets))
