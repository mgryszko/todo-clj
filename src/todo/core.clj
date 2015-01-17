(ns todo.core)

(defn create-todo [save-fn! task]
  (let [todo {:task task}]
    (save-fn! todo)))

