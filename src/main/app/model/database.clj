(ns app.model.database
  (:require [app.server-components.config :refer [config]]
            [mount.core :refer [defstate]]
            [hikari-cp.core :as pool]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as result-set]
            [next.jdbc.prepare :as p]
            [clojure.string :as str]
            [honeysql.core :as sql]
            [honeysql-postgres.format])
  (:import [java.net URI]))


(defn create-uri [url] (URI. url))

(defn parse-username-and-password [db-uri]
  (str/split (.getUserInfo db-uri) #":"))

(defn parse-db-name [db-uri]
  (str/replace (.getPath db-uri) "/" ""))

(defn parse-host [db-uri]
  (.getHost db-uri))

(defn parse-port [db-uri]
  (.getPort db-uri))

(defn hikari-connection-map
  "Converts Heroku's DATABASE_URL to a map that you can pass to hikari"
  [heroku-database-url]
  (let [db-uri (create-uri heroku-database-url)
        host    (parse-host db-uri)
        port    (parse-port db-uri)
        db-name (parse-db-name db-uri)
        [username password] (parse-username-and-password db-uri)]
    {:username      username
     :password      password
     :server-name   host
     :port-number   port
     :database-name db-name
     :sslmode       "require"}))

(defn process-database-spec [{:keys [jdbc-url] :as db-spec}]
  (if jdbc-url
    (hikari-connection-map jdbc-url)
    db-spec))

(defn datasource-options []
  (merge {:auto-commit        true
          :read-only          false
          :connection-timeout 30000
          :validation-timeout 5000
          :idle-timeout       600000
          :max-lifetime       1800000
          :minimum-idle       10
          :maximum-pool-size  10
          :pool-name          "db-pool"
          :adapter            "postgresql"
          :register-mbeans    false}
    (process-database-spec (:database-spec config))))

(defstate pool
  :start (pool/make-datasource (datasource-options))
  :stop (pool/close-datasource pool))

(defn snake->kebab [s]
  (str/replace s #"_" "-"))

(defn as-qualified-kebab-maps [rs opts]
  (result-set/as-modified-maps rs
    (assoc opts
      :qualifier-fn snake->kebab
      :label-fn snake->kebab)))

(def ^:private query-opts {:builder-fn as-qualified-kebab-maps})

(defn execute! [conn sql-map]
  (jdbc/execute! conn
    (sql/format sql-map :quoting :ansi)
    query-opts))

(defn execute-one! [conn sql-map]
  (jdbc/execute-one! conn
    (sql/format sql-map :quoting :ansi)
    query-opts))
