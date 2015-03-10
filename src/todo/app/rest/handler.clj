(ns todo.app.rest.handler
  (:require [compojure.core :refer [defroutes GET]] 
            [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [todo.core :as core]
            [todo.infrastructure.file.repository :as repo]))

(defn- find-all [_] (core/find-all-todos repo/find-all))

(defroutes app
  (GET "/todos" [] (resource :available-media-types ["application/json"]
                             :handle-ok find-all)))

(def handler 
  (-> app wrap-params))
