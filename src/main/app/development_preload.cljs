(ns app.development-preload
  (:require
    [taoensso.timbre :as log]
    [cljs.stacktrace :as st]
    [clojure.pprint :refer [pprint]]
    [clojure.string :as str]))

;; Add code to this file that should run when the initial application is loaded in development mode.
;; shadow-cljs already enables console print and plugs in devtools if they are on the classpath,

;; From timbre.  Modified to output better results from expound
(defn console-appender
  "Returns a simple js/console appender for ClojureScript.

  For accurate line numbers in Chrome, add these Blackbox[1] patterns:
    `/taoensso/timbre/appenders/core\\.js$`
    `/taoensso/timbre\\.js$`
    `/cljs/core\\.js$`

  [1] Ref. https://goo.gl/ZejSvR"

  ;; TODO Any way of using something like `Function.prototype.bind`
  ;; (Ref. https://goo.gl/IZzkQB) to get accurate line numbers in all
  ;; browsers w/o the need for Blackboxing?

  [& [opts]]
  {:enabled?   true
   :async?     false
   :min-level  nil
   :rate-limit nil
   :output-fn  :inherit
   :fn         (if (exists? js/console)
                 (let [;; Don't cache this; some libs dynamically replace js/console
                       level->logger
                       (fn [level]
                         (or
                           (case level
                             :trace js/console.trace
                             :debug js/console.debug
                             :info js/console.info
                             :warn js/console.warn
                             :error js/console.error
                             :fatal js/console.error
                             :report js/console.info)
                           js/console.log))]

                   (fn [{:keys [level vargs ?err output-fn] :as data}]
                     (when-let [logger (level->logger level)]
                       (let [output (when output-fn (output-fn (assoc data :msg_ "" :?err nil)))
                             ;; (<output> ?<raw-error> <raw-arg1> <raw-arg2> ...)
                             args   (if-let [err ?err]
                                      (cons output (cons err vargs))
                                      (cons output vargs))]
                         (.apply logger js/console (into-array args))
                         (when (instance? ExceptionInfo ?err)
                           (js/console.log (ex-message ?err)))))))
                 (fn [data] nil))})

(defn custom-output-fn
  "Mostly taken from timbre, but just formats message header as output.  The appender outputs args raw."
  ([data] (custom-output-fn nil data))
  ([opts data]                                              ; For partials
   (let [{:keys [level ?ns-str ?file ?line]} data]
     (str (str/upper-case (name level)) " " "[" (or ?ns-str ?file "?") ":" (or ?line "?") "] - "))))

(js/console.log "Turning logging to :debug (in app.development-preload)")
(log/set-level! :debug)
(log/merge-config! {:output-fn custom-output-fn
                    :appenders {:console (console-appender)}})
