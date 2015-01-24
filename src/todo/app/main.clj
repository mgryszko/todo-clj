(ns todo.app.main
  (require [clojure.string :refer [join blank?]]
           [todo.core :as core :refer [add-todo update-todo]]
           [todo.infrastructure.file.repository :as repo :refer [add-todo! update-todo!]])
  (:gen-class))

(defn as-complete-task [task-parts] (join " " task-parts))
(defn as-task [task-parts] (join " " task-parts))

(defn add [task-parts]
  (let [task (as-task task-parts)]
    (if (blank? task) 
      (println "Empty task!")
      (core/add-todo repo/add-todo! task))))

(defn update [[id & task-parts]]
  (let [task (as-task task-parts)]
    (if (blank? task)
      (println "Empty task!")
      (core/update-todo repo/update-todo! {:id id :task task}))))

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

