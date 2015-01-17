(defproject todo-clj "0.0.1-SNAPSHOT"
  :description "todo app"
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[midje "1.6.3" :exclusions  [org.clojure/clojure]]]
                   :plugins [[lein-midje "3.1.1"]]}})
