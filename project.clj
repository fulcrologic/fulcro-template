(defproject app "0.1.0-SNAPSHOT"
  :description "My Cool Project"
  :license {:name "MIT" :url "https://opensource.org/licenses/MIT"}
  :min-lein-version "2.8.1"

  ;; These "paths" directives are technical "duplicate" since the deps plugin will get them,
  ;; but needed or you end up with the default leiningen "src" and "test" on the classpath, which is bad.
  :source-paths ["src/main"]
  :test-paths ["src/test"]

  :plugins [[lein-tools-deps "0.4.1"]]
  :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  :lein-tools-deps/config {:config-files [:install :user :project]
                           :aliases [:dev]}

  :uberjar-name "app.jar"
  :profiles {:uberjar {:main           app.server-main
                       :aot            :all
                       :jar-exclusions [#"public/js/test" #"public/js/workspaces" #"public/workspaces.html"]
                       :prep-tasks     ["clean" ["clean"]
                                        "compile" ["run" "-m" "shadow.cljs.devtools.cli" "release" "main"]]}})

