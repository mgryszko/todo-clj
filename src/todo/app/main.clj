(ns todo.app.main
  (require [clojure.string :refer [join]]
           [todo.core :as core :refer [add-todo update-todo]]
           [todo.infrastructure.file.repository :as repo :refer [add-todo! update-todo!]])
  (:gen-class))

(defn as-complete-task [task-parts] (join " " task-parts))

(defn add [task-parts]
  (core/add-todo repo/add-todo! (as-complete-task task-parts)))

(defn update [[id & task-parts]]
  (core/update-todo repo/update-todo! {:id id :task (as-complete-task task-parts)}))

(def usage "
  Usage: todo action [task_number] [task_description]

    Actions:
      add \"task to be done\"
      update 1 \"task to be updated\"
")

(defn print-usage [_]
  (println usage))

(defn command-from-args [args]
  (case (first args)
    "add" add
    "update" update
    print-usage))

(defn -main [& args]
  (let [command (command-from-args args)]
    (command (rest args))))

