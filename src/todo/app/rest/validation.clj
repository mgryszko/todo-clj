(ns todo.app.rest.validation)

(defn valid? [[code]] (= code :ok))

(def ^{:private true} messages
  {:json-malformed "Unparseable JSON in body"
   :task-empty "Empty task"
   :id-not-found "No todo with number %s"})

(defn format-message [key & args]
  (apply format (into [(key messages)] args)))

