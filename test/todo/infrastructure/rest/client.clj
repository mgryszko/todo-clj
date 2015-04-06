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

(defn get-invalid-todo [id]
  (get-json (todos-url id) {:throw-exceptions false}))

(defn post-todo 
  ([todo] (post-json (todos-url) {:form-params todo})))

(defn post-invalid-todo [todo]
  (let [contains-todo? (not (empty? todo))
        request (merge {:throw-exceptions false} (if contains-todo? {:form-params todo}))]
    (post-json (todos-url) request)))

(defn put-todo [{:keys [id] :as todo}]
  (put-json (todos-url id) {:form-params todo}))

(defn put-invalid-todo [{:keys [id] :as todo}]
  (let [rest (dissoc todo :id)
        contains-todo? (not (empty? rest))
        request (merge {:throw-exceptions false} (if contains-todo? {:form-params todo}))]
    (put-json (todos-url id) request)))

(defn delete-todo [id]
  (delete-json (todos-url id)))

(defn delete-invalid-todo [id]
  (delete-json (todos-url id) {:throw-exceptions false}))

