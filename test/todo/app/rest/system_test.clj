(ns todo.app.rest.system-test
  (:require [clojure.data.json :as json]
            [clj-http.client :as http] 
            [midje.sweet :refer :all]
            [ring.server.standalone :as server]
            [todo.app.rest.handler :refer [handler]]
            [todo.app.rest.server :refer :all]
            [todo.infrastructure.file.repository :refer [add-todo!]]
            [todo.infrastructure.file.test-operations :refer [delete-todo-file]]))

(defn- send-json [method url req]
  (method url (merge {:content-type :json :as :json} (first req))))

(defn- get-json [url & req]
  (send-json http/get url req))

(defn- post-json [url & req]
  (send-json http/post url req))

(defn- put-json [url & req]
  (send-json http/put url req))

(defn- delete-json [url & req]
  (send-json http/delete url req))

(def port 3000)

(def base-todos-url (str "http://localhost:" port "/todos"))

(defn- todos-url
  ([] base-todos-url)
  ([id] (str base-todos-url "/" id)))

(defn- get-todos []
  (get-json (todos-url)))

(defn- get-todo [id]
  (get-json (todos-url id)))

(defn- post-todo 
  ([task] (post-json (todos-url) {:form-params {:task task}}))
  ([] (post-json (todos-url) {:throw-exceptions false})))

(defn- put-todo [todo]
  (put-json (todos-url (:id todo)) {:form-params todo}))

(defn- put-empty-todo [id]
  (put-json (todos-url id) {:throw-exceptions false}))

(defn- delete-todo [id]
  (delete-json (todos-url id)))

(defn- location [response]
  (get-in response [:headers :location]))

(defn- body-as-json [response]
  (let [body (:body response)]
    (if (string? body)
      (json/read-str body :key-fn keyword) 
      body)))

(against-background [(before :contents (start-server port)
                             :after (stop-server))
                     (before :facts [(delete-todo-file)
                                     (add-todo! {:task "first"})
                                     (add-todo! {:task "second"})
                                     (add-todo! {:task "third"})]
                             :after (delete-todo-file))]
  (facts "todo application"
    (fact "lists all todos"
      (let [response (get-todos)
            expected-todos [{:id 1 :task "first"} {:id 2 :task "second"} {:id 3 :task "third"}]]
        (:status response) => 200
        (body-as-json response) => expected-todos))
    
    (fact "lists single todo"
      (let [id 1
            response (get-todo id)]
        (:status response) => 200
        (body-as-json response) => {:id id :task "first"}))

    (fact "updates a todo"
      (let [id 1
            response (put-todo {:id id :task "first updated"})
            expected-todos [{:id 1 :task "first updated"} {:id 2 :task "second"} {:id 3 :task "third"}]]
      (:status response) => 200
      (body-as-json response) => {:id id :task "first updated"}
      (body-as-json (get-todos)) => expected-todos)) 

    (fact "updating without a body returns 400"
      (let [response (put-empty-todo 1)]
        (:status response) => 400 
        (body-as-json response) => (fn [actual] (contains? actual :message))))

    (fact "adds a todo"
      (let [response (post-todo "fourth")
            expected-id 4  
            expected-todos [{:id 1 :task "first"} {:id 2 :task "second"} {:id 3 :task "third"} {:id expected-id :task "fourth"}]]
        (:status response) => 201
        (location response) => (todos-url expected-id) 
        (body-as-json response) => {:id expected-id :task "fourth"}
        (body-as-json (get-todos)) => expected-todos))

    (fact "adding without a body returns 400"
      (let [response (post-todo)]
        (:status response) => 400 
        (body-as-json response) => (fn [actual] (contains? actual :message))))

    (fact "deletes a todo"
      (let [response (delete-todo 1)
            expected-todos [{:id 1 :task "second"} {:id 2 :task "third"}]]
        (:status response) => 204
        (body-as-json (get-todos)) => expected-todos))))

