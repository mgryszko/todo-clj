(defproject todo-clj "0.0.0-SNAPSHOT"
  :description "todo app"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [environ "1.0.0"]
                 [clj-stacktrace "0.2.8"]]
  :plugins [[lein-environ "1.0.0"]
            [lein-release "1.0.5"]]
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[midje "1.6.3" :exclusions [org.clojure/clojure]]]
                   :plugins [[lein-midje "3.1.1"]
                             [lein-bin "0.3.4"]]
                   :env {:todo-file "target/todo.txt"}}}
  :main todo.app.main
  :bin  {:name "todo" :bootclasspath true})
