(ns todo.app.rest.system-test
  (:require [clojure.data.json :as json]
            [clj-http.client :as http] 
            [midje.sweet :refer :all]
            [ring.server.standalone :as server]
            [todo.app.rest.handler :refer [handler]]
            [todo.app.rest.server :refer :all]
            [todo.infrastructure.file.repository :refer [add-todo!]]
            [todo.infrastructure.file.test-operations :refer :all]))

(def port 3000)

(def base-todos-url (str "http://localhost:" port "/todos"))

(defn- todos-url
  ([] base-todos-url)
  ([id] (str base-todos-url "/" id)))

(against-background [(before :contents (start-server port))
                     (after :contents (stop-server))
                     (before :facts (delete-todo-file) :after (delete-todo-file))]

  (facts "todo application"
    (against-background [(before :facts [(add-todo! {:task "first"})
                                         (add-todo! {:task "second"})
                                         (add-todo! {:task "third"})])]
      (fact "lists all todos"
        (let [response (http/get (todos-url) {:as :json})]
          (:status response) => 200
          (:body response) => [{:id 1 :task "first"} {:id 2 :task "second"} {:id 3 :task "third"}]))
      
      (fact "lists single todo"
        (let [id 1
              response (http/get (todos-url id) {:as :json})]
          (:status response) => 200
          (:body response) => {:id id :task "first"})))

    (fact "adds a todo"
      (let [response (http/post (todos-url)
                                {:form-params {:task "first"}
                                 :content-type :json
                                 :as :json})]
        (:status response) => 201
        (get-in response [:headers :location]) => (has-suffix "/todos/1") 
        (:body response) => {:id 1 :task "first"}))

    (fact "adding without a body returns 400"
      (let [response (http/post (todos-url)
                                {:throw-exceptions false
                                 :as :json})]
        (:status response) => 400 
        (json/read-str (:body response) :key-fn keyword) => (fn [actual] (contains? actual :message))))

    (against-background [(before :facts [(add-todo! {:task "first"})])]
      (fact "updates a todo"
        (let [id 1
              response (http/put (todos-url id)
                                 {:form-params {:task "first updated"}
                                  :content-type :json
                                  :as :json})]
        (:status response) => 200
        (:body response) => {:id id :task "first updated"})))))

