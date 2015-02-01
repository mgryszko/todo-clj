(ns todo.core
  (require [clojure.string :refer [blank?]]))

(defmacro proceed-if [validate-fn]
  `(let [validation-result# ~validate-fn]
     (if (not (= (first validation-result#) :ok))
       (throw (IllegalArgumentException. (str "Precondition failed: " validation-result#))))))

(defn can-todo-be-added? [task]
  (if (blank? task) [:task_empty] [:ok]))

(defn add-todo [save-fn! task]
  (proceed-if (can-todo-be-added? task))
  (let [todo {:task task}]
    (save-fn! todo)))

(defn can-todo-be-updated? [id-exists-fn {:keys [id task]}]
  (cond
    (not (id-exists-fn id)) [:id_not_found id]
    (blank? task) [:task_empty]
    :else [:ok]))

(defn update-todo [id-exists-fn save-fn! todo]
  (proceed-if (can-todo-be-updated? id-exists-fn todo))
  (save-fn! todo))
