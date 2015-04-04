(ns todo.app.rest.handler
  (:require [clojure.data.json :as json]
            [compojure.core :refer [defroutes GET POST PUT DELETE]] 
            [liberator.core :refer [resource]]
            [ring.middleware.params :refer [wrap-params]]
            [todo.app.rest.validation :refer :all]
            [todo.core :as core]
            [todo.infrastructure.file.repository :as repo]
            [todo.infrastructure.parse :as parse])
  (:import [java.net URL]))

(defn- find-all [_]
  (core/find-all-todos repo/find-all))

(defn- find-by-id [string-id]
  (core/find-todo-by-id repo/find-by-line-number (parse/->int string-id)))

(defn- body-as-string [ctx]
  (let [body (get-in ctx [:request :body])]
    (slurp (clojure.java.io/reader body))))

(defn- body-as-json [ctx]
  (-> (body-as-string ctx)
      (json/read-str :key-fn keyword)))

(defn- parse-json [ctx]
  (try 
    [false {::data (body-as-json ctx)}]
    (catch Exception e [true {:representation {:media-type "application/json"}}])))

(defn- build-entry-url [request id]
  (URL. (format "%s://%s:%s%s/%d"
                (name (:scheme request))
                (:server-name request)
                (:server-port request)
                (:uri request)
                id)))

(defn- todo-location [ctx]
  (build-entry-url (:request ctx) (get-in ctx [::todo :id])))

(defn- add [ctx]
  (let [data (::data ctx)]
    (let [todo (core/add-todo repo/add-todo! (:task data))]
      {::todo todo})))

(defn- update-processable? [ctx string-id]
  (let [data (::data ctx)
        todo (assoc data :id (parse/->int string-id))
        validation-result (core/can-todo-be-updated? repo/line-num-exists? todo)]
     (if (valid? validation-result)
       [true {::todo todo}]
       [false {::validation-result validation-result}])))

(defn- update [ctx]
  (let [todo (::todo ctx)]
    {::todo (core/update-todo repo/line-num-exists? repo/update-todo! todo)}))

(defn- delete [ctx string-id]
  (let [id (parse/->int string-id)]
    (core/delete-todo repo/line-num-exists? repo/delete-todo! id)))

(defn- error-entity [ctx]
  (let [[code args] (::validation-result ctx)] 
    {:code code
     :message (format-message code args)}))

(defroutes app
  (GET "/todos" [] (resource :available-media-types ["application/json"]
                             :handle-ok find-all))

  (GET "/todos/:id" [id] (resource :available-media-types ["application/json"]
                                   :handle-ok (fn [_] (find-by-id id))))

  (POST "/todos" [] (resource :allowed-methods [:post]
                              :available-media-types ["application/json"]
                              :malformed? parse-json
                              :handle-malformed {:message "Unparseable JSON"}
                              :post! add
                              :location todo-location
                              :handle-created ::todo))

  (PUT "/todos/:id" [id] (resource :allowed-methods [:put]
                                   :available-media-types ["application/json"]
                                   :malformed? parse-json
                                   :handle-malformed {:message "Unparseable JSON"}
                                   :processable? (fn [ctx] (update-processable? ctx id))
                                   :handle-unprocessable-entity error-entity
                                   :put! update
                                   :new? false
                                   :respond-with-entity? true
                                   :handle-ok ::todo))

  (DELETE "/todos/:id" [id] (resource :allowed-methods [:delete]
                                      :delete! (fn [ctx] (delete ctx id)))))

(def handler 
  (-> app wrap-params))
