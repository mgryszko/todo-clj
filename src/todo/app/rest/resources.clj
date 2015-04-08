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

(defn- error-entity [{[code args] ::validation-result}]
  {:code code
   :message (format-message code args)})

(defn- find-all [_]
  (core/find-all-todos repo/find-all))

(defn- find-by-id [string-id]
  (if-let [todo (core/find-todo-by-id repo/find-by-line-number (parse/->int string-id))]
    {::todo todo}
    [false {::validation-result [:id-not-found string-id]}]))

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
  {::todo (core/add-todo repo/add-todo! task)})

(defn- update-processable? [{data ::data} string-id]
  (let [todo (assoc data :id (parse/->int string-id))
        validation-result (core/can-todo-be-updated? repo/line-num-exists? todo)]
     (if (ok-or-not-found? validation-result)
       {::todo todo ::validation-result validation-result}
       [false {::validation-result validation-result}])))

(defn- update-exists? [{validation-result ::validation-result}]
    (found? validation-result))

(defn- update [{todo ::todo}]
  {::todo (core/update-todo repo/line-num-exists? repo/update-todo! todo)})

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
  :location todo-location
  :handle-created ::todo)

(defresource put-todo [id]
  :allowed-methods [:put]
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
  :handle-ok ::todo)

(defresource delete-todo [id]
  :allowed-methods [:delete]
  :malformed? [false json-representation] 
  :exists? (fn [_] (delete-processable? id)) 
  :handle-not-found error-entity
  :delete! delete)

