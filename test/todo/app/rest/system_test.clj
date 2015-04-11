(ns todo.app.rest.system-test
  (:require [midje.sweet :refer :all]
            [todo.app.rest.client :refer :all]
            [todo.app.rest.server :refer :all]
            [todo.infrastructure.file.test-operations :refer [delete-todo-file]]))

(def existing-id 1)
(def non-existing-id 100)
(def non-numeric-id "one")
(def empty-task " \t\n\r ")

(def ok 200)
(def created 201)
(def no-content 204)
(def bad-request 400)
(def not-found 404)
(def unprocessable-entity 422)

(def json-malformed-entity {:code "json-malformed" :message "Unparseable JSON in body"})
(defn id-not-found-entity [id] {:code "id-not-found" :message (format "No todo with number %s" id)})
(def task-empty-entity {:code "task-empty" :message "Empty task"})

(defn add-initial-todos []
  (post-todo {:task "first"})
  (post-todo {:task "second"})
  (post-todo {:task "third"}))

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
            (:status response) => ok
            (:body response) => expected-todos))
       
        (fact "single by id"
          (let [response (get-todo existing-id)]
            (:status response) => ok
            (:body response) => {:id existing-id :task "first"}))

        (fact "with 404 error when todo id doesn't exist"
          (let [response (get-invalid-todo non-existing-id)]
            (:status response) => not-found
            (:body response) => (id-not-found-entity non-existing-id))))

    (facts "updates a todo"

      (fact "successfully"
        (let [response (put-todo {:id existing-id :task "first updated"})
              expected-todos [{:id existing-id :task "first updated"} {:id 2 :task "second"} {:id 3 :task "third"}]]
        (:status response) => ok
        (:body response) => {:id existing-id :task "first updated"}
        (:body (get-todos)) => expected-todos)) 

      (fact "with 400 error when no body"
        (let [response (put-invalid-todo {:id existing-id})]
          (:status response) => bad-request
          (:body response) => json-malformed-entity))
      
      (fact "with 404 error when todo id doesn't exist"
        (let [response (put-invalid-todo {:id non-existing-id :task "updated"})]
          (:status response) => not-found
          (:body response) => (id-not-found-entity non-existing-id)))

      (fact "with 404 error when todo id is non-numeric"
        (let [response (put-invalid-todo {:id non-numeric-id :task "updated"})]
          (:status response) => not-found
          (:body response) => (id-not-found-entity non-numeric-id)))

      (fact "with 422 error when task is empty"
        (let [response (put-invalid-todo {:id existing-id :task empty-task})]
          (:status response) => unprocessable-entity
          (:body response) => task-empty-entity)))

    (facts "adds a todo"

      (fact "successfully"
        (let [response (post-todo {:task "fourth"})
              expected-id 4  
              expected-todos [{:id 1 :task "first"} {:id 2 :task "second"} {:id 3 :task "third"} {:id expected-id :task "fourth"}]]
          (:status response) => created
          (:location response) => (todos-url expected-id) 
          (:body response) => {:id expected-id :task "fourth"}
          (:body (get-todos)) => expected-todos))

      (fact "with 400 error when no body"
        (let [response (post-invalid-todo {})]
          (:status response) => bad-request
          (:body response) => json-malformed-entity))
      
      (fact "with 422 error when task is empty"
        (let [response (post-invalid-todo {:task empty-task})]
          (:status response) => unprocessable-entity
          (:body response) => task-empty-entity)))

    (facts "deletes a todo" 

      (fact "successfully"
        (let [response (delete-todo existing-id)
              expected-todos [{:id 1 :task "second"} {:id 2 :task "third"}]]
          (:status response) => no-content
          (:body (get-todos)) => expected-todos))
      
      (fact "with 404 error when todo id doesn't exist"
        (let [response (delete-invalid-todo non-existing-id)]
          (:status response) => not-found
          (:body response) => (id-not-found-entity non-existing-id)))

      (fact "with 404 error when todo id is non-numeric"
        (let [response (delete-invalid-todo non-numeric-id)]
          (:status response) => not-found
          (:body response) => (id-not-found-entity non-numeric-id))))))

