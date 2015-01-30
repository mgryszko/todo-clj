(ns todo.app.main
  (require [clojure.string :refer [join blank?]]
           [todo.core :as core :refer [can-todo-be-added? add-todo update-todo]]
           [todo.infrastructure.file.operations :as ops :refer [count-lines]]
           [todo.infrastructure.file.repository :as repo :refer [add-todo! update-todo!]])
  (:gen-class))

(def messages {:task_empty "Empty task!"})

(defn print-message [key] (println (key messages)))

(defmacro proceed-if [validate-fn on-success]
  `(let [validation-result# ~validate-fn]
     (if (= validation-result# :ok)
       ~on-success
       (print-message validation-result#))))

(defn as-task [task-parts] (join " " task-parts))

(defn add [task-parts]
  (let [task (as-task task-parts)]
    (proceed-if (can-todo-be-added? task)
      (core/add-todo repo/add-todo! task))))

(defn update [[id & task-parts]]
  (let [task (as-task task-parts)]
    (cond 
      (blank? task)
        (println "Empty task!")
      (or (> id (ops/count-lines "todo.txt")) (< id 1))
        (println (str "No task with number " id "!"))
      :else
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

