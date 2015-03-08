(ns todo.app.rest.handler
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes GET]]))

(defroutes app
  (GET "/todos" [] (resource :available-media-types ["application/json"]
                             :handle-ok [{:id 1 :task "first"} {:id 2 :task "second"} {:id 3 :task "third"}])))

(def handler 
  (-> app wrap-params))
