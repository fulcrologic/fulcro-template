(ns app.server-components.pathom-wrappers
  (:require
    [clojure.spec.alpha :as s]
    [com.fulcrologic.fulcro.algorithms.misc :as util]
    [taoensso.timbre :as log]
    [com.wsscode.pathom.connect :as pc]))

(defonce pathom-registry (atom {}))
(defn register! [resolver]
  (log/debug "Registering resolver " (::pc/sym resolver))
  (swap! pathom-registry assoc (::pc/sym resolver) resolver))

(s/def ::mutation-args (s/cat
                         :sym simple-symbol?
                         :doc (s/? string?)
                         :arglist vector?
                         :config map?
                         :body (s/* any?)))

;; This is the macro syntax generator for resolvers and mutations, so you can add a security layer (e.g. based on the
;; config passed in) to your resolvers and mutations easily here, and it can have access to anything in the
;; parsing environment and query/tx at runtime.
(defn defpathom-endpoint* [endpoint args]
  (let [{:keys [sym arglist doc config body]} (util/conform! ::mutation-args args)
        config     (dissoc config :security)
        env-arg    (first arglist)
        params-arg (second arglist)]
    `(do
       (~endpoint ~(cond-> sym
                     doc (with-meta {:doc doc})) [env# params#]
         ~config
         ;; Example of how to integrate a security check into all mutations and resolvers
         (let [~env-arg env#
               ~params-arg params#]
           ~@body))
       (app.server-components.pathom-wrappers/register! ~sym))))

(defmacro ^{:doc      "Defines a server-side PATHOM mutation. This macro can be \"resolved as\" defn for IDE recognition.

Example:

(defmutation do-thing
  \"Optional docstring\"
  [params]
  {::pc/input [:param/name]  ; PATHOM config
   ::pc/output [:result/prop]}
  body)
                      "
            :arglists '([sym docstring? arglist config & body])} defmutation
  [& args]
  (defpathom-endpoint* `pc/defmutation args))

(defmacro ^{:doc      "Defines a pathom resolver but with authorization. Looks like `defn`.

Example:

(defresolver resolver-name [env input]
  {::pc/input [:customer/id]
   ...}
  {:customer/name \"Bob\"})
"
            :arglists '([sym docstring? arglist config & body])} defresolver
  [& args]
  (defpathom-endpoint* `pc/defresolver args))
