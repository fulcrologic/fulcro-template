(ns user
  (:require
    [clojure.tools.namespace.repl :as tools-ns :refer [set-refresh-dirs]]
    [expound.alpha :as expound]
    [clojure.spec.alpha :as s]
    [mount.core :as mount]
    ;; this is the top-level dependent component...mount will find the rest via ns requires
    [app.server-components.http-server :refer [http-server]]))

;; ==================== SERVER ====================
;; Ensure we only refresh the source we care about. This is important
;; because `resources` is on our classpath and we don't want to
;; accidentally pull source from there when cljs builds cache files there.
(set-refresh-dirs "src/main" "src/dev" "src/test")
;; Change the default output of spec to be more readable
(alter-var-root #'s/*explain-out* (constantly expound/printer))


(defn start
  "Start the web server"
  [] (mount/start))

(defn stop
  "Stop the web server"
  [] (mount/stop))

(defn restart
  "Stop, reload code, and restart the server. If there is a compile error, use:

  ```
  (tools-ns/refresh)
  ```

  to recompile, and then use `start` once things are good."
  []
  (stop)
  (tools-ns/refresh :after 'user/start))


;; These are here so we can run them from the editor with kb shortcuts.  See IntelliJ's "Send Top Form To REPL" in
;; keymap settings.
(comment
  (start)
  (restart))


;; ==================== CLIENT ====================
(comment
  (require '[shadow.cljs.devtools.api :as shadow])
  (shadow/repl :main))
