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
    (fact "lists todos"
      (let [response (http/get "http://localhost:3000/todos" {:as :json})]
        (:status response) => 200
        (:body response) => [{:id 1 :task "first"} {:id 2 :task "second"} {:id 3 :task "third"}]))))

