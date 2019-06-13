(ns app.client
  (:require
    [com.fulcrologic.fulcro.application :as app]
    [app.ui.root :as root]
    [com.fulcrologic.fulcro.networking.http-remote :as net]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [taoensso.timbre :as log]))

(def secured-request-middleware
  ;; The CSRF token is embedded via server_components/html.clj
  (->
    (net/wrap-csrf-token (or js/fulcro_network_csrf_token "TOKEN-NOT-IN-HTML!"))
    (net/wrap-fulcro-request)))


(defonce SPA (app/fulcro-app
               {
                ;; This ensures your client can talk to a CSRF-protected server.
                ;; See middleware.clj to see how the token is embedded into the HTML
                :remotes          {:remote (net/fulcro-http-remote
                                             {:url                "/api"
                                              :request-middleware secured-request-middleware})}}))

(defn ^:export init []
  (log/info "Remount")
  (app/mount! SPA root/Root "app"))
