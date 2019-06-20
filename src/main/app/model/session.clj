(ns app.model.session
  (:require
    [app.model.mock-database :as db]
    [datascript.core :as d]
    [ghostwheel.core :refer [>defn => | ?]]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    [clojure.spec.alpha :as s]
    [com.fulcrologic.fulcro.server.api-middleware :as fmw]))

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

(defmutation login [env {:keys [username password]}]
  {::pc/symbol `login
   ::pc/output [:session/valid? :account/name]}
  (log/info "Authenticating" username)
  (if (and (seq username) (= password "letmein"))
    (response-updating-session env
      {:session/valid? true
       :account/name   username})
    (do
      (log/error "Invalid credentials supplied for" username)
      (throw (ex-info "Invalid credentials" {:username username})))))

(defmutation logout [env params]
  {::pc/symbol `login
   ::pc/output [:session/valid?]}
  (response-updating-session env {:session/valid? false :account/name ""}))

(def resolvers [current-session-resolver login logout])
