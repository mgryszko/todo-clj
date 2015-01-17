(ns todo.core)

(defn create-todo [save-fn! task]
  (let [todo {:task task}]
    (save-fn! todo)))

(defn update-todo [save-fn! todo]
  (save-fn! todo)) 
