(defproject todo-clj "0.1.0-SNAPSHOT"
  :description "todo app"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [environ "1.0.0"]
                 [liberator "0.12.2"]
                 [compojure "1.3.2"]
                 [ring/ring-core "1.3.2"]]
  :plugins [[lein-environ "1.0.0"]
            [lein-ring "0.9.2"]]
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[midje "1.6.3" :exclusions [org.clojure/clojure]]
                                  [clj-stacktrace "0.2.8"]
                                  [clj-http "1.0.1"]
                                  [ring-server "0.4.0"]]
                   :plugins [[lein-midje "3.1.1"]
                             [lein-bin "0.3.4"]]
                   :env {:todo-file "target/todo.txt"}}}
  :repl-options {:init (do
                         (use 'midje.repl) 
                         (autotest))}
  :main todo.infrastructure.cli.main
  :bin  {:name "todo" :bootclasspath true}
  :ring {:handler todo.infrastructure.rest.handler/handler})
