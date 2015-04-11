(ns todo.infrastructure.cli.main
  (:require [clojure.string :refer [join]]
            [clj-stacktrace.repl :as stacktrace]
            [todo.core :as core]
            [todo.infrastructure.cli.validation :as val]
            [todo.infrastructure.file.repository :as repo]
            [todo.infrastructure.parse :as parse])
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

(defn- update [[string-id & task-parts]]
  (let [id (parse/->int string-id)
        task (as-task task-parts)
        todo {:id id :task task}]
    (val/proceed-if (core/can-todo-be-updated? repo/line-num-exists? todo)
      (let [old-todo (core/find-todo-by-id repo/find-by-line-number id)]
      (->> (core/update-todo repo/line-num-exists? repo/update-todo! todo)
           (print-formatted (str "Updated: " (format-todo old-todo) "\n     to: ")))))))

(defn- delete [[string-id]]
  (let [id (parse/->int string-id)]
    (val/proceed-if (core/can-todo-be-deleted? repo/line-num-exists? id)
      (->> (core/delete-todo repo/line-num-exists? repo/delete-todo! id)
        (print-formatted "Deleted: ")))))

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

(defn- command-from-args [[command]]
  (case command
    "add" add
    "update" update
    "delete" delete 
    "list" find-all
    print-usage))

(def handler (reify Thread$UncaughtExceptionHandler
  (uncaughtException [_ _ e]
    (if (instance? java.io.IOException e)
      (println "Unexpected file error occurred. Verify your todo file the requested operation was performed."))
    (stacktrace/pst-on *err* false e))))

(defn -main [& args]
  (Thread/setDefaultUncaughtExceptionHandler handler)
  (let [command (command-from-args args)]
    (command (rest args))))

