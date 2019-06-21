(ns app.client
  (:require
    [app.application :refer [SPA]]
    [com.fulcrologic.fulcro.application :as app]
    [app.ui.root :as root]
    [com.fulcrologic.fulcro.networking.http-remote :as net]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro-css.css-injection :as cssi]
    [app.model.session :as session]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]))

(defn ^:export refresh []
  (log/info "Hot code Remount")
  (cssi/upsert-css "componentcss" {:component root/Root})
  (app/mount! SPA root/Root "app"))

(defn ^:export init []
  (log/info "Application starting.")
  (app/mount! SPA root/Root "app")
  (cssi/upsert-css "componentcss" {:component root/Root})
  (log/info "Starting session machine.")
  (uism/begin! SPA session/session-machine ::session/session
    {:actor/login-form      root/Login
     :actor/current-session root/Session}))


(comment
  (let [s (app/current-state SPA)]
    (fdn/db->tree [{[:component/id :login] [:ui/open? :ui/error :account/email
                                            {[:root/current-session '_] (comp/get-query root/Session)}
                                            [::uism/asm-id ::session/session]]}] {} s))
  )
