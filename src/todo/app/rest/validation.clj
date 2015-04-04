(ns todo.app.rest.validation)

(defn valid? [[code]] (= code :ok))

(def ^{:private true} messages
  {:task_empty "Empty task"
   :id_not_found "No todo with number %s"})

(defn format-message [key & args]
  (apply format (into [(key messages)] args)))

