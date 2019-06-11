(ns app.model.user
  (:require
    [com.fulcrologic.fulcro.algorithms.merge :refer [integrate-ident*]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.algorithms.tx-processing :as txn]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp]))

(defn user-path
  "Normalized path to a user entity or field in Fulcro state-map"
  ([id field] [:user/id id field])
  ([id] [:user/id id]))

(defn insert-user*
  "Insert a user into the correct table of the Fulcro state-map database."
  [state-map {:user/keys [id] :as user}]
  (assoc-in state-map (user-path id) user))

(defmutation upsert-user
  "Client Mutation: Upsert a user (full-stack. see CLJ version for server-side)."
  [{:user/keys [id name] :as params}]
  (action [{:keys [state]}]
    (swap! state (fn [s]
                   (-> s
                     (insert-user* params)
                     (integrate-ident* [:user/id id] :append [:all-users])))))
  (result-action [{:keys [result state app] :as env}]
    (let [{:keys [status-code body]} result
          user (get body 'app.model.user/upsert-user)
          {:keys [returning] :as tx-options} (::txn/options env)]
      (log/info (keys env) body (comp/component-name returning))
      (when (= 200 status-code)
        (swap! state merge/merge-component returning user)
        (app/schedule-render! app))))
  (remote [env] true))
