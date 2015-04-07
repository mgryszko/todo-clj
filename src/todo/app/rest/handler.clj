(ns todo.app.rest.handler
  (:require [clojure.data.json :as json]
            [compojure.core :refer [defroutes GET POST PUT DELETE]] 
            [liberator.core :refer [resource]]
            [liberator.representation :refer [as-response ring-response]]
            [ring.middleware.params :refer [wrap-params]]
            [todo.app.rest.validation :refer :all]
            [todo.core :as core]
            [todo.infrastructure.file.repository :as repo]
            [todo.infrastructure.parse :as parse])
  (:import [java.net URL]))

(defn- find-all [_]
  (core/find-all-todos repo/find-all))

(defn- todo-exists? [string-id]
  (if-let [todo (core/find-todo-by-id repo/find-by-line-number (parse/->int string-id))]
    {::todo todo}
    [false {::validation-result [:id-not-found string-id]}]))

(defn- body-as-string [{{:keys [body]} :request}]
  (slurp (clojure.java.io/reader body)))

(defn- body-as-json [ctx]
  (-> (body-as-string ctx)
      (json/read-str :key-fn keyword)))

(def ^{:private true} fixed-representation {:representation {:media-type "application/json"}}) 

(defn- error-entity [{[code args] ::validation-result}]
  {:code code
   :message (format-message code args)})

(defn- parse-json [ctx]
  (try 
    [false {::data (body-as-json ctx)}]
    (catch Exception _ (assoc fixed-representation ::validation-result [:json-malformed]))))

(defn- todo-location [{:keys [request] {:keys [id]} ::todo}]
  (URL. (format "%s://%s:%s%s/%d"
                (name (:scheme request))
                (:server-name request)
                (:server-port request)
                (:uri request)
                id)))

(defn- add-processable? [{{task :task} ::data :as all}]
  (let [validation-result (core/can-todo-be-added? task)]
     (if (valid? validation-result)
       {::task task}
       [false {::validation-result validation-result}])))

(defn- add [{task ::task}]
  (let [todo (core/add-todo repo/add-todo! task)]
    {::todo todo}))

(defn- update-processable? [{data ::data} string-id]
  (let [todo (assoc data :id (parse/->int string-id))
        validation-result (core/can-todo-be-updated? repo/line-num-exists? todo)]
     (if (ok-or-not-found? validation-result)
       {::todo todo ::validation-result validation-result}
       [false {::validation-result validation-result}])))

(defn- update [{todo ::todo}]
  {::todo (core/update-todo repo/line-num-exists? repo/update-todo! todo)})

(defn- update-exists? [{validation-result ::validation-result}]
    (found? validation-result))

(defn- update-change-501-to-404 [ctx]
   (-> (as-response (error-entity ctx) ctx)
     (assoc :status 404)
     (ring-response)))

(defn- delete-processable? [string-id]
  (let [id (parse/->int string-id)
        validation-result (core/can-todo-be-deleted? repo/line-num-exists? id)]
    (if (valid? validation-result)
      {::id id}
      [false {::validation-result validation-result}])))

(defn- delete [{id ::id}]
  (core/delete-todo repo/line-num-exists? repo/delete-todo! id))

(defroutes app
  (GET "/todos" [] (resource :available-media-types ["application/json"]
                             :handle-ok find-all))

  (GET "/todos/:id" [id] (resource :available-media-types ["application/json"]
                                   :malformed? [false fixed-representation] 
                                   :exists? (fn [_] (todo-exists? id)) 
                                   :handle-not-found error-entity
                                   :handle-ok ::todo))

  (POST "/todos" [] (resource :allowed-methods [:post]
                              :available-media-types ["application/json"]
                              :malformed? parse-json
                              :handle-malformed error-entity
                              :processable? add-processable?
                              :handle-unprocessable-entity error-entity
                              :post! add
                              :location todo-location
                              :handle-created ::todo))

  (PUT "/todos/:id" [id] (resource :allowed-methods [:put]
                                   :available-media-types ["application/json"]
                                   :malformed? parse-json
                                   :handle-malformed error-entity
                                   :processable? (fn [ctx] (update-processable? ctx id))
                                   :handle-unprocessable-entity error-entity
                                   :exists? update-exists?
                                   :can-put-to-missing? false
                                   :handle-not-implemented update-change-501-to-404
                                   :put! update
                                   :new? false
                                   :respond-with-entity? true
                                   :handle-ok ::todo))

  (DELETE "/todos/:id" [id] (resource :allowed-methods [:delete]
                                      :malformed? [false fixed-representation] 
                                      :processable? (fn [_] (delete-processable? id))
                                      :handle-unprocessable-entity error-entity
                                      :delete! delete)))

(def handler 
  (-> app wrap-params))
