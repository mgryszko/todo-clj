(ns todo.infrastructure.rest.client
  (:require [clojure.data.json :as json]
            [clj-http.client :as http]))

(defn- location [response]
  (get-in response [:headers :location]))

(defn- body-as-json [{:keys [body]}]
  (if (string? body)
    (json/read-str body :key-fn keyword) 
    body))

(defn- send-json [method url [req]]
  (let [response (method url (merge {:content-type :json :as :json} req))]
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

(def ^{:private true} base-todos-url (str "http://localhost:" port "/todos"))

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

(defn put-todo [{:keys [id] :as todo}]
  (put-json (todos-url id) {:form-params todo}))

(defn put-invalid-todo [{:keys [id] :as todo}]
  (let [rest (dissoc todo :id)
        contains-todo? (not (empty? rest))
        request (merge {:throw-exceptions false} (if contains-todo? {:form-params todo}))]
    (put-json (todos-url id) request)))

(defn delete-todo [id]
  (delete-json (todos-url id)))

