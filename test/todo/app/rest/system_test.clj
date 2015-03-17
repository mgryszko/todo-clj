(ns todo.app.rest.system-test
  (:require [clj-http.client :as http] 
            [midje.sweet :refer :all]
            [ring.server.standalone :as server]
            [todo.app.rest.handler :refer [handler]]
            [todo.app.rest.server :refer :all]
            [todo.infrastructure.file.repository :refer [add-todo!]]
            [todo.infrastructure.file.test-operations :refer :all]))

(def port 3000)

(against-background [(before :contents (start-server port))
                     (after :contents (stop-server))
                     (before :facts (delete-todo-file)) 
                     (after :facts (delete-todo-file))]

  (facts "todo application"
    (against-background [(before :facts [(add-todo! {:task "first"})
                                         (add-todo! {:task "second"})
                                         (add-todo! {:task "third"})])]
      (fact "lists todos"
        (let [response (http/get "http://localhost:3000/todos" {:as :json})]
          (:status response) => 200
          (:body response) => [{:id 1 :task "first"} {:id 2 :task "second"} {:id 3 :task "third"}])))

    (fact "adds a todo"
      (let [response (http/post "http://localhost:3000/todos"
                                {:form-params {:task "first"}
                                 :content-type :json
                                 :as :json})]
        (:status response) => 201
        (:body response) => {:id 1 :task "first"}))

    (against-background [(before :facts [(add-todo! {:task "first"})])]
      (fact "updates a todo"
        (let [response (http/put "http://localhost:3000/todos"
                                {:form-params {:id 1 :task "first updated"}
                                 :content-type :json
                                 :as :json})]
        (:status response) => 200
        (:body response) => {:id 1 :task "first updated"})))))

