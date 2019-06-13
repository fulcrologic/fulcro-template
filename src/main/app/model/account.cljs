(ns app.model.account
  (:require
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]))

(defn user-path
  "Normalized path to a user entity or field in Fulcro state-map"
  ([id field] [:account/id id field])
  ([id] [:account/id id]))

(defn insert-user*
  "Insert a user into the correct table of the Fulcro state-map database."
  [state-map {:keys [:account/id] :as user}]
  (assoc-in state-map (user-path id) user))

(defmutation upsert-user
  "Client Mutation: Upsert a user (full-stack. see CLJ version for server-side)."
  [{:keys [:account/id :account/name] :as params}]
  (action [{:keys [state]}]
    (log/info "Upsert user action")
    (swap! state (fn [s]
                   (-> s
                     (insert-user* params)
                     (merge/integrate-ident* [:account/id id] :append [:all-accounts])))))
  (ok-action [env]
    (log/info "OK action"))
  (error-action [env]
    (log/info "Error action"))
  (remote [env]
    (-> env
      (m/returning 'app.ui.root/User)
      (m/with-target (targeting/append-to [:all-accounts])))))
