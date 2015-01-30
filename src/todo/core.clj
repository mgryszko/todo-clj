(ns todo.core
  (require [clojure.string :refer [blank?]]))

(defmacro proceed-if [validate-fn]
  `(let [validation-result# ~validate-fn]
     (if (not (= validation-result# :ok))
       (throw (IllegalArgumentException. (str "Precondition failed: " validation-result#))))))

(defn can-todo-be-added? [task]
  (if (blank? task) :task_empty :ok))

(defn add-todo [save-fn! task]
  (proceed-if (can-todo-be-added? task))
  (let [todo {:task task}]
    (save-fn! todo)))

(defn update-todo [save-fn! todo]
  (save-fn! todo)) 
