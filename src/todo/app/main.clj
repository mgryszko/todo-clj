(ns todo.app.main
  (require [clojure.string :refer [join]]
           [todo.app.validation :as val]
           [todo.core :as core]
           [todo.infrastructure.file.repository :as repo])
  (:gen-class))

(defn- as-task [task-parts] (join " " task-parts))

(defn- format-todo [{:keys [id task]}]
  (str id " " task))

(defn- add [task-parts]
  (let [task (as-task task-parts)]
    (val/proceed-if (core/can-todo-be-added? task)
      (->> (core/add-todo repo/add-todo! task)
           (format-todo)
           (str "Added: ")
           (println)))))

(defn- update [[id & task-parts]]
  (let [task (as-task task-parts)
        todo {:id id :task task}]
    (val/proceed-if (core/can-todo-be-updated? repo/id-exists? todo)
      (->> (core/update-todo repo/id-exists? repo/update-todo! todo)
        (format-todo)
        (str "Updated: ")
        (println)))))

(defn- find-all [_]
  (->> (core/find-all-todos repo/find-all)
       (map format-todo)
       (join "\n")
       (println)))

(defn- print-usage [_]
  (println "
  Usage: todo action [task_number] [task_description]

    Actions:
      add \"task to be done\"
      update 1 \"task to be updated\"
      list
"))

(defn- command-from-args [args]
  (case (first args)
    "add" add
    "update" update
    "list" find-all
    print-usage))

(defn -main [& args]
  (let [command (command-from-args args)]
    (command (rest args))))

