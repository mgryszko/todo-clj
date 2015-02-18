(ns todo.app.main
  (require [clojure.string :refer [join]]
           [todo.app.validation :as val]
           [todo.core :as core]
           [todo.infrastructure.file.repository :as repo])
  (:gen-class))

(defn- as-task [task-parts] (join " " task-parts))

(defn- format-todo [{:keys [id task]}]
  (str id " " task))

(defn- print-formatted [text todo]
  (->> (format-todo todo)
       (str text)
       (println)))

(defn- add [task-parts]
  (let [task (as-task task-parts)]
    (val/proceed-if (core/can-todo-be-added? task)
      (->> (core/add-todo repo/add-todo! task)
           (print-formatted "Added: ")))))

(defn- update [[id & task-parts]]
  (let [task (as-task task-parts)
        todo {:id id :task task}]
    (val/proceed-if (core/can-todo-be-updated? repo/line-num-exists? todo)
      (let [old-todo (core/find-todo-by-id repo/find-by-line-number id)]
      (->> (core/update-todo repo/line-num-exists? repo/update-todo! todo)
           (print-formatted (str "Updated: " (format-todo old-todo) "\n     to: ")))))))

(defn- delete [[id]]
  (->> (core/delete-todo repo/delete-todo! id)
       (print-formatted "Deleted: ")))

(defn- find-all [_]
  (let [todos (core/find-all-todos repo/find-all)]
    (dorun (map #(print-formatted "" %) todos))))

(defn- print-usage [_]
  (println "
  Usage: todo action [task_number] [task_description]

    Actions:
      add \"task to be done\"
      update 1 \"task to be updated\"
      delete 2
      list
"))

(defn- command-from-args [args]
  (case (first args)
    "add" add
    "update" update
    "delete" delete 
    "list" find-all
    print-usage))

(defn -main [& args]
  (let [command (command-from-args args)]
    (command (rest args))))

