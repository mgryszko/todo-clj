(ns todo.app.rest.handler
  (:require [clojure.data.json :as json]
            [compojure.core :refer [defroutes GET POST PUT]] 
            [liberator.core :refer [resource]]
            [ring.middleware.params :refer [wrap-params]]
            [todo.core :as core]
            [todo.infrastructure.file.repository :as repo])
  (:import [java.net URL]))

(defn- find-all [_]
  (core/find-all-todos repo/find-all))

(defn- find-by-id [id]
  (core/find-todo-by-id repo/find-by-line-number (Integer/parseInt id)))

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

(defn- todo-as-json-str [ctx]
  (json/write-str (::todo ctx)))

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

(defn- update [ctx id]
  (let [body (body-as-json ctx)
        todo (assoc body :id (Integer/parseInt id))]
    {::todo (core/update-todo repo/line-num-exists? repo/update-todo! todo)}))

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
                                   :media-type-available? true
                                   :put! (fn [ctx] (update ctx id))
                                   :new? false
                                   :respond-with-entity? true
                                   :handle-ok todo-as-json-str)))

(def handler 
  (-> app wrap-params))
