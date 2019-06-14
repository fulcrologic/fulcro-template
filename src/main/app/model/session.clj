(ns app.model.session
  (:require
    [app.model.mock-database :as db]
    [datascript.core :as d]
    [ghostwheel.core :refer [>defn => | ?]]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    [clojure.spec.alpha :as s]))

(defresolver current-session-resolver [env input]
  {::pc/output [{::current-session [:session/valid? :account/name]}]}
  (log/info "Resolving current session")
  ;; TODO: Add real session support (using defaults middleware)
  {::current-session {:session/valid? false}})

(defmutation login [env {:keys [username password]}]
  {::pc/symbol `login
   ::pc/output [:session/valid? :account/name]}
  (log/info "Authenticating" username)
  (if (= password "letmein")
    {:session/valid? true
     :account/name   "Joe"}
    {:session/valid? false
     :account/name   ""}))

(def resolvers [current-session-resolver login])


