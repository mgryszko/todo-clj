(ns todo.core)

(defn add-todo [save-fn! task]
  (let [todo {:task task}]
    (save-fn! todo)))

(defn update-todo [save-fn! todo]
  (save-fn! todo)) 
