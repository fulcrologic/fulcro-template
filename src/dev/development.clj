(ns development
  (:require
    [app.server-components.http-server]
    [clojure.tools.namespace.repl :as tools-ns]
    [mount.core :as mount]))

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
  (tools-ns/refresh :after 'development/start))

(comment
  (start)
  (restart))
