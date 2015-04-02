(ns todo.infrastructure.rest.client
  (:require [clojure.data.json :as json]
            [clj-http.client :as http]))

(defn- location [response]
  (get-in response [:headers :location]))

(defn- body-as-json [response]
  (let [body (:body response)]
    (if (string? body)
      (json/read-str body :key-fn keyword) 
      body)))

(defn- send-json [method url req]
  (let [response (method url (merge {:content-type :json :as :json} (first req)))]
    {:status (:status response)
     :body (body-as-json response)
     :location (location response)}))

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

(defn todos-url
  ([] base-todos-url)
  ([id] (str base-todos-url "/" id)))

(defn get-todos []
  (get-json (todos-url)))

(defn get-todo [id]
  (get-json (todos-url id)))

(defn post-todo 
  ([task] (post-json (todos-url) {:form-params {:task task}}))
  ([] (post-json (todos-url) {:throw-exceptions false})))

(defn put-todo [todo]
  (put-json (todos-url (:id todo)) {:form-params todo}))

(defn put-empty-todo [id]
  (put-json (todos-url id) {:throw-exceptions false}))

(defn delete-todo [id]
  (delete-json (todos-url id)))

