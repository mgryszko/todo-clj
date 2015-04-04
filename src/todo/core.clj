(ns todo.core
  (require [clojure.string :refer [blank?]]))

(defmacro proceed-if [validate-fn]
  `(let [validation-result# ~validate-fn]
     (if (not (= (first validation-result#) :ok))
       (throw (IllegalArgumentException. (str "Precondition failed: " validation-result#))))))

(defn can-todo-be-added? [task]
  (if (blank? task) [:task-empty] [:ok]))

(defn add-todo [save-fn! task]
  (proceed-if (can-todo-be-added? task))
  (let [todo {:task task}]
    (save-fn! todo)))

(defn can-todo-be-updated? [id-exists-fn {:keys [id task]}]
  (cond
    (not (id-exists-fn id)) [:id-not-found id]
    (blank? task) [:task-empty]
    :else [:ok]))

(defn update-todo [id-exists-fn save-fn! todo]
  (proceed-if (can-todo-be-updated? id-exists-fn todo))
  (save-fn! todo))

(defn can-todo-be-deleted? [id-exists-fn id]
  (cond
    (not id) [:id-mandatory]
    (not (id-exists-fn id)) [:id-not-found id]
    :else [:ok]))

(defn delete-todo [id-exists-fn delete-fn! id]
  (proceed-if (can-todo-be-deleted? id-exists-fn id))
  (delete-fn! id))

(defn find-todo-by-id [find-by-id-fn id] (find-by-id-fn id))

(defn find-all-todos [find-all-fn] (find-all-fn))
