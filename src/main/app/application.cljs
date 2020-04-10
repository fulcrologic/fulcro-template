(ns app.application
  (:require [com.fulcrologic.fulcro.networking.http-remote :as net]
            [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.components :as comp]
            [com.fulcrologic.fulcro.networking.websockets :as fws]))

(def secured-request-middleware
  ;; The CSRF token is embedded via server_components/html.clj
  (->
    (net/wrap-csrf-token (or js/fulcro_network_csrf_token "TOKEN-NOT-IN-HTML!"))
    (net/wrap-fulcro-request)))

;; TODO: Sort out websockets csrf
(def secured-request-ws-params
  ;; The CSRF token is embedded via server_components/html.clj
  {:csrf-token (or js/fulcro_network_csrf_token "TOKEN-NOT-IN-HTML!")})

(defonce SPA (app/fulcro-app
               {;; This ensures your client can talk to a CSRF-protected server.
                ;; See middleware.clj to see how the token is embedded into the HTML
                :remotes {:remote (net/fulcro-http-remote
                                    {:url                "/api"
                                     :request-middleware secured-request-middleware})
                          :ws-remote (fws/fulcro-websocket-remote {} #_secured-request-ws-params)}}))

(comment
  (-> SPA (::app/runtime-atom) deref ::app/indexes))
