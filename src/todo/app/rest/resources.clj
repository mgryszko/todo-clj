(ns todo.app.rest.resources
  (:require [clojure.data.json :as json]
            [liberator.core :refer [defresource]]
            [liberator.representation :refer [as-response ring-response]]
            [todo.app.rest.json :refer :all]
            [todo.app.rest.validation :refer :all]
            [todo.core :as core]
            [todo.infrastructure.file.repository :as repo]
            [todo.infrastructure.parse :as parse])
  (:import [java.net URL]))

(def ^{:private true} json-representation {:representation {:media-type "application/json"}}) 

(defn- parse-json [ctx]
  (try 
    [false {::data (body-as-json ctx)}]
    (catch Exception _ (assoc json-representation ::validation-result [:json-malformed]))))

(defmacro if-valid? [validate-fn on-success]
  `(let [validation-result# ~validate-fn] 
    (if (valid? validation-result#)
      ~on-success
      [false {::validation-result validation-result#}])))

(defn- error-entity [{[code args] ::validation-result}]
  {:code code
   :message (format-message code args)})

(defn- force-404-status [ctx]
   (-> (as-response (error-entity ctx) ctx)
     (assoc :status 404)
     (ring-response)))

(defn- find-all [_]
  (core/find-all-todos repo/find-all))

(defn- find-by-id [string-id]
  (if-let [todo (core/find-todo-by-id repo/find-by-line-number (parse/->int string-id))]
    {::todo todo}
    [false {::validation-result [:id-not-found string-id]}]))

(defn- add-processable? [{{task :task} ::data :as all}]
  (if-valid? (core/can-todo-be-added? task)
    {::task task}))

(defn- add [{task ::task}]
  {::todo (core/add-todo repo/add-todo! task)})

(defn- added-todo-location [{:keys [request] {:keys [id]} ::todo}]
  (URL. (format "%s://%s:%s%s/%d"
                (name (:scheme request))
                (:server-name request)
                (:server-port request)
                (:uri request)
                id)))

(defn- update-processable? [{data ::data} string-id]
  (let [todo (assoc data :id (parse/->int string-id))]
     (if-valid? (core/can-todo-be-updated? repo/line-num-exists? todo)
       {::todo todo})))

(defn- update [{todo ::todo}]
  {::todo (core/update-todo repo/line-num-exists? repo/update-todo! todo)})

(defn- handle-unprocessable-update [ctx]
  (if (not-found? (::validation-result ctx))
    (force-404-status ctx)
    (error-entity ctx)))

(defn- delete-processable? [string-id]
  (let [id (parse/->int string-id)]
    (if-valid? (core/can-todo-be-deleted? repo/line-num-exists? id)
      {::id id})))

(defn- delete [{id ::id}]
  (core/delete-todo repo/line-num-exists? repo/delete-todo! id))

(defresource get-todos
  :available-media-types ["application/json"]
  :handle-ok find-all)

(defresource get-todo [id]
  :available-media-types ["application/json"]
  :malformed? [false json-representation] 
  :exists? (fn [_] (find-by-id id)) 
  :handle-not-found error-entity
  :handle-ok ::todo)

(defresource post-todo
  :allowed-methods [:post]
  :available-media-types ["application/json"]
  :malformed? parse-json
  :handle-malformed error-entity
  :processable? add-processable?
  :handle-unprocessable-entity error-entity
  :post! add
  :location added-todo-location
  :handle-created ::todo)

(defresource put-todo [id]
  :allowed-methods [:put]
  :available-media-types ["application/json"]
  :malformed? parse-json
  :handle-malformed error-entity
  :processable? (fn [ctx] (update-processable? ctx id))
  :handle-unprocessable-entity handle-unprocessable-update 
  :put! update
  :new? false
  :respond-with-entity? true
  :handle-ok ::todo)

(defresource delete-todo [id]
  :allowed-methods [:delete]
  :malformed? [false json-representation] 
  :exists? (fn [_] (delete-processable? id)) 
  :handle-not-found error-entity
  :delete! delete)

