(ns todo.app.rest.handler
  (:require [clojure.data.json :as json]
            [compojure.core :refer [defroutes GET POST]] 
            [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [todo.core :as core]
            [todo.infrastructure.file.repository :as repo]))

(defn- find-all [_]
  (core/find-all-todos repo/find-all))

(defn- body-as-string [ctx]
  (let [body (get-in ctx [:request :body])]
    (slurp (clojure.java.io/reader body))))

(defn- body-as-json [ctx]
  (-> (body-as-string ctx)
      (json/read-str :key-fn keyword)))

(defn- todo-as-json-str [ctx]
  (json/write-str (::todo ctx)))

(defn- add [ctx]
  (let [body (body-as-json ctx)]
    (let [todo (core/add-todo repo/add-todo! (:task body))]
      {::todo todo})))

(defroutes app
  (GET "/todos" [] (resource :available-media-types ["application/json"]
                             :handle-ok find-all))
  (POST "/todos" [] (resource :allowed-methods [:post]
                              :post! add
                              :handle-created todo-as-json-str)))

(def handler 
  (-> app wrap-params))
