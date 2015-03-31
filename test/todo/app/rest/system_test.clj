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

(def port 3000)

(def base-todos-url (str "http://localhost:" port "/todos"))

(defn- todos-url
  ([] base-todos-url)
  ([id] (str base-todos-url "/" id)))

(defn- get-todos
  ([] (get-json (todos-url)))
  ([id] (get-json (todos-url id))))

(defn- post-todo 
  ([task] (post-json (todos-url) {:form-params {:task task}}))
  ([] (post-json (todos-url) {:throw-exceptions false})))

(defn- put-todo [todo]
  (put-json (todos-url (:id todo)) {:form-params todo}))

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
      (let [response (get-todos)]
        (:status response) => 200
        (body-as-json response) => [{:id 1 :task "first"} {:id 2 :task "second"} {:id 3 :task "third"}]))
    
    (fact "lists single todo"
      (let [id 1
            response (get-todos 1)]
        (:status response) => 200
        (body-as-json response) => {:id id :task "first"}))

    (fact "updates a todo"
      (let [id 1
            response (put-todo {:id id :task "first updated"})]
      (:status response) => 200
      (body-as-json response) => {:id id :task "first updated"})) 

    (fact "adds a todo"
      (let [expected-id 4 
            response (post-todo "first")]
        (:status response) => 201
        (location response) => (todos-url expected-id) 
        (body-as-json response) => {:id expected-id :task "first"}))

    (fact "adding without a body returns 400"
      (let [response (post-todo)]
        (:status response) => 400 
        (body-as-json response) => (fn [actual] (contains? actual :message))))))

