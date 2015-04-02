(ns todo.app.rest.system-test
  (:require [midje.sweet :refer :all]
            [todo.infrastructure.rest.server :refer :all]
            [todo.infrastructure.file.repository :refer [add-todo!]]
            [todo.infrastructure.file.test-operations :refer [delete-todo-file]]
            [todo.infrastructure.rest.client :refer :all]))

(against-background [(before :contents (start-server port)
                             :after (stop-server))
                     (before :facts [(delete-todo-file)
                                     (add-todo! {:task "first"})
                                     (add-todo! {:task "second"})
                                     (add-todo! {:task "third"})]
                             :after (delete-todo-file))]
  (facts "todo application"

    (facts "lists todos"
       (fact "all"
          (let [response (get-todos)
                expected-todos [{:id 1 :task "first"} {:id 2 :task "second"} {:id 3 :task "third"}]]
            (:status response) => 200
            (:body response) => expected-todos))
       
        (fact "single"
          (let [id 1
                response (get-todo id)]
            (:status response) => 200
            (:body response) => {:id id :task "first"})))
        

    (facts "updates a todo"

      (fact "successfully"
        (let [id 1
              response (put-todo {:id id :task "first updated"})
              expected-todos [{:id 1 :task "first updated"} {:id 2 :task "second"} {:id 3 :task "third"}]]
        (:status response) => 200
        (:body response) => {:id id :task "first updated"}
        (:body (get-todos)) => expected-todos)) 

      (fact "without a body returns 400"
        (let [response (put-empty-todo 1)]
          (:status response) => 400 
          (:body response) => (fn [actual] (contains? actual :message)))))

    (facts "adds a todo"

      (fact "successfully"
        (let [response (post-todo "fourth")
              expected-id 4  
              expected-todos [{:id 1 :task "first"} {:id 2 :task "second"} {:id 3 :task "third"} {:id expected-id :task "fourth"}]]
          (:status response) => 201
          (:location response) => (todos-url expected-id) 
          (:body response) => {:id expected-id :task "fourth"}
          (:body (get-todos)) => expected-todos))

      (fact "adding without a body returns 400"
        (let [response (post-todo)]
          (:status response) => 400 
          (:body response) => (fn [actual] (contains? actual :message)))))

    (facts "deletes a todo" 

      (fact "successfully"
        (let [response (delete-todo 1)
              expected-todos [{:id 1 :task "second"} {:id 2 :task "third"}]]
          (:status response) => 204
          (:body (get-todos)) => expected-todos)))))

