(ns app.application
  (:require
    [com.fulcrologic.fulcro.networking.http-remote :as net]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp]
    [edn-query-language.core :as eql]))

(def secured-request-middleware
  ;; The CSRF token is embedded via server_components/html.clj
  (->
    (net/wrap-csrf-token (or js/fulcro_network_csrf_token "TOKEN-NOT-IN-HTML!"))
    (net/wrap-fulcro-request)))

(defn global-eql-transform
  "As the default transform but also asking that any Pathom errors during load! are returned,
  so that they can be inspected e.g. in `:remote-error?`"
  [ast]
  (cond-> (app/default-global-eql-transform ast)
    (-> ast :type #{:root})
    (update :children conj (eql/expr->ast :com.wsscode.pathom.core/errors))))

(defonce SPA (app/fulcro-app
               {;; This ensures your client can talk to a CSRF-protected server.
                ;; See middleware.clj to see how the token is embedded into the HTML
                :remotes {:remote (net/fulcro-http-remote
                                    {:url                "/api"
                                     :request-middleware secured-request-middleware})}
                :global-eql-transform global-eql-transform}))

(comment
  (-> SPA (::app/runtime-atom) deref ::app/indexes))
