(ns app.server-components.session
  (:require [mount.core :refer [defstate]]
            [ring.middleware.session.store :as store]
            [ring.middleware.session.memory :refer [memory-store]]))


(defstate mem-store
  :start (memory-store))

(defn ws-lookup [key]
  (store/read-session mem-store key))

(defn get-session [env]
  (condp #(get-in %2 %1) env
    [:request :session/key]  ;; websockets path
    (some-> env :request :session/key ws-lookup)
    [:ring/request]          ;; http path
    (some-> env :ring/request :session)
    nil))
