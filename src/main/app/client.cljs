(ns app.client
  (:require
    [app.application :refer [SPA]]
    [com.fulcrologic.fulcro.application :as app]
    [app.ui.root :as root]
    [com.fulcrologic.fulcro.networking.http-remote :as net]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [app.model.session :as session]
    [taoensso.timbre :as log]))

(defn ^:export refresh []
  (log/info "Hot code Remount")
  (app/mount! SPA root/Root "app"))

(defn ^:export init []
  (log/info "Application starting.")
  (app/mount! SPA root/Root "app")
  (log/info "Starting session machine.")
  (uism/begin! SPA session/session-machine ::session/session
    {:actor/login-form      root/Login
     :actor/current-session root/Session}))
