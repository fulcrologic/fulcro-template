(ns app.model.session
  (:require
    [app.application :refer [SPA]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h3 button]]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m]))

(defn clear [env]
  (uism/assoc-aliased env :busy? false :error ""))

(defn logout [env]
  (-> env
    (clear)
    (uism/assoc-aliased :username "" :password "" :session-valid? false :current-user "")
    (uism/activate :state/logged-out)))

(defn login [{::uism/keys [event-data] :as env}]
  (-> env
    (clear)
    (uism/assoc-aliased :busy? true)
    (uism/trigger-remote-mutation :actor/login-form 'app.model.session/login {:username          (:username event-data)
                                                                              :password          (:password event-data)
                                                                              ::m/returning      (uism/actor-class env :actor/current-session)
                                                                              ::uism/ok-event    :event/complete
                                                                              ::uism/error-event :event/failed})))

(defn process-session-result [env error-message]
  (let [success? (uism/alias-value env :session-valid?)]
    (when success?
      (dr/change-route SPA ["main"]))
    (cond-> (clear env)
      success? (uism/activate :state/logged-in)
      (not success?) (->
                       (uism/assoc-aliased :error error-message)
                       (uism/activate :state/logged-out)))))

(uism/defstatemachine session-machine
  {::uism/actors
   #{:actor/login-form :actor/current-session}

   ::uism/aliases
   {:username       [:actor/login-form :account/email]
    :busy?          [:actor/login-form :ui/loading?]
    :error          [:actor/login-form :ui/error]
    :session-valid? [:actor/current-session :session/valid?]
    :current-user   [:actor/current-session :account/name]}

   ::uism/states
   {:initial
    {::uism/target-states #{:state/checking-session}
     ::uism/handler       (fn [env]
                            (-> env
                              (uism/assoc-aliased :busy? true :error "")
                              (uism/set-timeout :network-timer :event/timeout {} 2000 #{:event/complete})
                              (uism/load ::current-session :actor/current-session
                                {::uism/ok-event    :event/complete
                                 ::uism/error-event :event/failed})
                              (uism/activate :state/checking-session)))}

    :state/checking-session
    {::uism/events {:event/timeout  {::uism/target-states #{:state/logged-out}
                                     ::uism/handler       (fn [env]
                                                            (-> env
                                                              (clear)
                                                              (logout)
                                                              (uism/assoc-aliased :error "Server unavailable.")))}

                    :event/failed   {::uism/target-states #{:state/logged-out}
                                     ::uism/handler       (fn [env]
                                                            (-> env
                                                              (clear)
                                                              (uism/assoc-aliased :error "Server error.")))}
                    :event/complete {::uism/handler #(process-session-result % "")}}}

    :state/logged-in
    {::uism/events {:event/logout {::uism/target-states #{:state/logged-out}
                                   ::uism/target-state  :state/logged-out
                                   ::uism/handler       logout}}}

    :state/logged-out
    {::uism/events {:event/login    {::uism/target-states #{:state/logged-out :state/logged-in}
                                     ::uism/handler       login}
                    :event/failed   {::uism/hander (fn [env] (-> env clear (uism/assoc-aliased :error "Server error. Try again later.")))}
                    :event/complete {::uism/handler #(process-session-result % "Invalid credentials.")}}}}})

(defsc Session [this {:keys [:session/valid? :account/name] :as props}]
  {:query         [:session/valid? :account/name]
   :ident         (fn [] [:component/id :session])
   :pre-merge     (fn [{:keys [data-tree]}]
                    (log/info "pre merge" data-tree)
                    (merge {:session/valid? false :account/name ""}
                      data-tree))
   :initial-state {:session/valid? false :account/name ""}}
  (dom/div
    (if valid?
      (dom/div :.ui.button name)
      (dom/button :.ui.button {:onClick (fn [] (dr/change-route this ["login"]))} "Login"))))

(def ui-session (prim/factory Session {:keyfn :session/valid?}))

