(ns app.model.session
  (:require
    [app.model.database :as db]
    [datascript.core :as d]
    [com.fulcrologic.guardrails.core :refer [>defn => | ?]]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    [clojure.spec.alpha :as s]
    [buddy.hashers :as hs]
    [com.fulcrologic.fulcro.server.api-middleware :as fmw]))

(defonce account-database (atom {}))

(defresolver current-session-resolver [env input]
  {::pc/output [{::current-session [:session/valid? :account/name]}]}
  (let [{:keys [account/name session/valid?]} (get-in env [:ring/request :session])]
    (if valid?
      (do
        (log/info name "already logged in!")
        {::current-session {:session/valid? true :account/name name}})
      {::current-session {:session/valid? false}})))

(defn response-updating-session
  "Uses `mutation-response` as the actual return value for a mutation, but also stores the data into the (cookie-based) session."
  [mutation-env mutation-response]
  (let [existing-session (some-> mutation-env :ring/request :session)]
    (fmw/augment-response
      mutation-response
      (fn [resp]
        (let [new-session (merge existing-session mutation-response)]
          (assoc resp :session new-session))))))

(defmutation login [{::db/keys [pool] :as env} {:keys [username password]}]
  {::pc/output [:session/valid? :account/name]}
  (log/info "Authenticating" username)
  (let [user (db/execute-one! pool
                              {:select [:email :password]
                               :from   [:account]
                               :where  [:= :email username]})
        {expected-email    :account/email
         expected-password :account/password} user]
    (if (and (= username expected-email) (hs/check password expected-password))
      (response-updating-session env
        {:session/valid? true
         :account/name   username})
      (do
        (log/error "Invalid credentials supplied for" username)
        (throw (ex-info "Invalid credentials" {:username username}))))))

(defmutation logout [env params]
  {::pc/output [:session/valid?]}
  (response-updating-session env {:session/valid? false :account/name ""}))

(defmutation signup! [{::db/keys [pool] :as env} {:keys [email password]}]
  {::pc/output [:signup/result]}
  (log/info "Signing Up" email)
  (let [hashed-password (hs/derive password)]
    (db/execute-one! pool
                     {:insert-into :account
                      :values [{:email email
                                :password hashed-password}]
                      :returning [:id]})
    {:signup/result "OK"}))

(def resolvers [current-session-resolver login logout signup!])
