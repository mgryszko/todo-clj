(ns todo.app.rest.system-test
  (:require [midje.sweet :refer :all]
            [todo.infrastructure.rest.server :refer :all]
            [todo.infrastructure.file.repository :refer [add-todo!]]
            [todo.infrastructure.file.test-operations :refer [delete-todo-file]]
            [todo.infrastructure.rest.client :refer :all]))

(def empty-task " \t\n\r ")
(def existing-id 1)
(def non-existing-id 100)
(def non-numeric-id "one")

(defn add-initial-todos []
  (add-todo! {:task "first"})
  (add-todo! {:task "second"})
  (add-todo! {:task "third"}))

(against-background [(before :contents (start-server port)
                             :after (stop-server))
                     (before :facts [(delete-todo-file)
                                     (add-initial-todos)]
                             :after (delete-todo-file))]
  (facts "todo application"

    (facts "lists todos"
       (fact "all"
          (let [response (get-todos)
                expected-todos [{:id 1 :task "first"} {:id 2 :task "second"} {:id 3 :task "third"}]]
            (:status response) => 200
            (:body response) => expected-todos))
       
        (fact "single"
          (let [response (get-todo existing-id)]
            (:status response) => 200
            (:body response) => {:id existing-id :task "first"})))

    (facts "updates a todo"

      (fact "successfully"
        (let [response (put-todo {:id existing-id :task "first updated"})
              expected-todos [{:id existing-id :task "first updated"} {:id 2 :task "second"} {:id 3 :task "third"}]]
        (:status response) => 200
        (:body response) => {:id existing-id :task "first updated"}
        (:body (get-todos)) => expected-todos)) 

      (fact "with 400 error when no body"
        (let [response (put-invalid-todo {:id existing-id})]
          (:status response) => 400 
          (:body response) => {:code "json-malformed" :message "Unparseable JSON in body"}))
      
      (fact "with 422 error when todo id doesn't exist"
        (let [response (put-invalid-todo {:id non-existing-id :task "updated"})]
          (:status response) => 422
          (:body response) => {:code "id-not-found" :message "No todo with number 100"}))

      (fact "with 422 error when todo id is non-numeric"
        (let [response (put-invalid-todo {:id non-numeric-id :task "updated"})]
          (:status response) => 422
          (:body response) => {:code "id-not-found" :message "No todo with number one"}))

      (fact "with 422 error when task is empty"
        (let [response (put-invalid-todo {:id existing-id :task empty-task})]
          (:status response) => 422
          (:body response) => {:code "task-empty" :message "Empty task"})))

    (facts "adds a todo"

      (fact "successfully"
        (let [response (post-todo {:task "fourth"})
              expected-id 4  
              expected-todos [{:id 1 :task "first"} {:id 2 :task "second"} {:id 3 :task "third"} {:id expected-id :task "fourth"}]]
          (:status response) => 201
          (:location response) => (todos-url expected-id) 
          (:body response) => {:id expected-id :task "fourth"}
          (:body (get-todos)) => expected-todos))

      (fact "with 400 error when no body"
        (let [response (post-invalid-todo {})]
          (:status response) => 400 
          (:body response) => {:code "json-malformed" :message "Unparseable JSON in body"}))
      
      (fact "with 422 error when task is empty"
        (let [response (post-invalid-todo {:task empty-task})]
          (:status response) => 422 
          (:body response) => {:code "task-empty" :message "Empty task"})))

    (facts "deletes a todo" 

      (fact "successfully"
        (let [response (delete-todo existing-id)
              expected-todos [{:id 1 :task "second"} {:id 2 :task "third"}]]
          (:status response) => 204
          (:body (get-todos)) => expected-todos))
      
      (fact "with 422 error when todo id doesn't exist"
        (let [response (delete-invalid-todo non-existing-id)]
          (:status response) => 422
          (:body response) => {:code "id-not-found" :message "No todo with number 100"}))

      (fact "with 422 error when todo id is non-numeric"
        (let [response (delete-invalid-todo non-numeric-id)]
          (:status response) => 422
          (:body response) => {:code "id-not-found" :message "No todo with number one"})))))

