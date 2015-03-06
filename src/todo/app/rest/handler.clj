(ns todo.app.rest.handler
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer  [wrap-params]]
            [compojure.core :refer [defroutes ANY]]))

(defroutes app
    (ANY "/foo" [] (resource :available-media-types ["text/plain"]
                             :handle-ok "Hello world!")))

(def handler 
  (-> app wrap-params))
