(ns app.model.account-test
  (:require
    [app.server-components.pathom :refer [build-parser]]
    [app.model.account :as acct]
    [app.util :refer [uuid]]
    [clojure.test :refer [deftest is]]
    [fulcro-spec.core :refer [specification provided behavior assertions component provided!]]
    [app.model.mock-database :as db]
    [datascript.core :as d]
    [taoensso.timbre :as log]))

(defn seeded-setup []
  (let [conn (db/new-database)]
    (d/transact conn [{:account/id (uuid 1) :account/active? false}
                      {:account/id (uuid 2) :account/active? true :account/email "account@example.net"}
                      {:account/id (uuid 3) :account/active? true}])
    {:conn conn
     :db   @conn}))

(deftest all-account-ids-test
  (let [{:keys [db]} (seeded-setup)
        ids (acct/all-account-ids db)]
    (assertions
      "can find the active account IDs that are in the database given"
      (set ids) => #{(uuid 2) (uuid 3)})))

(deftest get-account-test
  (let [{:keys [db]} (seeded-setup)
        entity (acct/get-account db (uuid 2) [:account/email])]
    (assertions
      "can find the requested account details"
      entity => {:account/email "account@example.net"})))

(deftest parser-integration-test
  (component "The pathom parser for the server"
    (let [{:keys [conn]} (seeded-setup)
          parser (build-parser conn)]
      (assertions
        "Pulls details for all active accounts"
        (parser {} [{:all-accounts [:account/email]}])
        => {:all-accounts [{}
                           {:account/email "account@example.net"}]})))

  (provided! "The database contains the account"
    (acct/get-account db uuid subquery) => (select-keys
                                             {:account/id      uuid
                                              :account/active? false
                                              :account/cruft   22
                                              :account/email   "boo@bah.com"} subquery)

    (component "The pathom parser for the server"
      (let [{:keys [conn]} (seeded-setup)
            parser (build-parser conn)]
        (assertions
          "Can pull the details of an account"
          (parser {} [{[:account/id (uuid 2)] [:account/id :account/email :account/active?]}])
          => {[:account/id (uuid 2)] {:account/id      (uuid 2)
                                      :account/email   "boo@bah.com"
                                      :account/active? false}})))))
