(ns todo.app.rest.handler
  (:require [compojure.core :refer [defroutes GET POST PUT DELETE]] 
            [ring.middleware.params :refer [wrap-params]]
            [todo.app.rest.resources :refer :all]))

(defroutes app
  (GET "/todos" [] get-todos)
  (GET "/todos/:id" [id] (get-todo id))
  (POST "/todos" [] post-todo)
  (PUT "/todos/:id" [id] (put-todo id))
  (DELETE "/todos/:id" [id] (delete-todo id)))

(def handler 
  (-> app wrap-params))
