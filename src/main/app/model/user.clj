(ns app.model.user
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]))

(def user-database (atom {}))

(defresolver all-users-resolver [env input]
  {;;GIVEN nothing
   ::pc/output [{:all-users [:user/id]}]}                   ;; I can output all users. NOTE: only ID is needed...other resolvers resolve the rest
  (log/info "All users. Database contains: " @user-database)
  {:all-users (mapv
                (fn [id] {:user/id id})
                (keys @user-database))})

(defresolver user-resolver [env {:user/keys [id]}]
  {::pc/input  #{:user/id}                                  ; GIVEN a user ID
   ::pc/output [:user/name]}                                ; I can produce a user's details
  ;; Look up the user (e.g. in a database), and return what you promised
  (when (contains? @user-database id)
    (get @user-database id)))

(defresolver user-address-resolver [env {:user/keys [id]}]
  {::pc/input  #{:user/id}                                  ; GIVEN a user ID
   ::pc/output [:address/id :address/street :address/city :address/state :address/postal-code]} ; I can produce address details
  (log/info "Resolving address for " id)
  {:address/id          "fake-id"
   :address/street      "111 Main St."
   :address/city        "Nowhere"
   :address/state       "WI"
   :address/postal-code "99999"})

(defmutation upsert-user [{:keys [config ring/request]} {:user/keys [id name]}]
  {::pc/params #{:user/id :user/name}
   ::pc/output [:user/id]}
  (when (and id name)
    ;; simulate some network delay so you can see the diff between optimistic and network response
    (Thread/sleep 500)
    (swap! user-database assoc id {:user/id   id
                                   :user/name name})
    ;; Returning the user id allows the UI to query for the result. In this case we're "virtually" adding an address for
    ;; them!
    {:user/id id}))

(def resolvers [all-users-resolver user-resolver user-address-resolver upsert-user])
