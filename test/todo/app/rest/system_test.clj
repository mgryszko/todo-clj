(ns todo.app.rest.system-test
  (:require [clj-http.client :as http] 
            [midje.sweet :refer :all]
            [ring.server.standalone :as server]
            [todo.app.rest.handler :refer [handler]]))

(defonce server (atom nil))

(defn start-server []
  (reset! server
    (server/serve handler {:port 3000 :open-browser? false :auto-reload? false})))

(defn stop-server []
  (.stop @server)
  (reset! server nil))

(against-background [(before :contents (start-server))
                     (after :contents (stop-server))]
  (facts "todo application"
    (fact "responds with hello world" 
      (let [result (http/get "http://localhost:3000/hello")]
        (:status result) => 200))))

