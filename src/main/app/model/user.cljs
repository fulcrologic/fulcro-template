(ns app.model.user
  (:require
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]))

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
                       (merge/integrate-ident* [:user/id id] :append [:all-users])))))
  (remote [env]
    (-> env
      (m/returning 'app.ui.root/User)
      (m/with-target (targeting/append-to [:all-users])))))
