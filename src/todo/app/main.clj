(ns todo.app.main
  (require [clojure.string :refer [join]]
           [todo.core :refer [create-todo update-todo]]
           [todo.infrastructure.file.repository :refer [add-todo! update-todo!]])
  (:gen-class))

(defn as-complete-task [task-parts] (join " " task-parts))

(defn add [task-parts]
  (create-todo add-todo! (as-complete-task task-parts)))

(defn update [[id & task-parts]]
  (update-todo update-todo! {:id id :task (as-complete-task task-parts)}))

(defn no-idea [_]
  (println "no idea what to do"))

(defn command-from-args [args]
  (case (first args)
    "add" add
    "update" update
    no-idea))

(defn -main [& args]
  (let [command (command-from-args args)]
    (command (rest args))))

