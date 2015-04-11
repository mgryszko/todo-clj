(ns todo.app.cli.system-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [midje.sweet :refer :all]
            [todo.infrastructure.cli.main :refer [-main]]
            [todo.infrastructure.file.test-operations :refer [delete-todo-file]]))

(defmacro with-main [command & args]
  `(str/trim (with-out-str (-main ~command ~@args))))

(defn add-silently [& tasks]
  (doall (map #(with-main "add" %) tasks)))

(defn add-initial-todos []
  (add-silently "first" "second" "third"))

(def empty-task " \t\n\r ")
(def non-existing-id 100)
(def non-numeric-id "one")

(defn not-found-msg [id] (format "No todo with number %s!" id))
(def empty-task-msg "Empty task!")

(defn list-todos []
  (str/split-lines (with-main "list")))

(against-background [(before :facts [(delete-todo-file)
                                     (add-initial-todos)]
                             :after (delete-todo-file))]
  (facts "todo application"

    (fact "lists todos"
      (list-todos) => ["1 first" "2 second" "3 third"])

    (facts "updates a todo"

      (fact "successfully"
        (with-main "update" 1 "first updated") => "Updated: 1 first\n     to: 1 first updated"  
        (with-main "update" 3 "third updated") => "Updated: 3 third\n     to: 3 third updated"
        (list-todos) => ["1 first updated" "2 second" "3 third updated"])

      (fact "rejecting  with a nonexisting todo id"
        (with-main "update" non-existing-id) => (not-found-msg non-existing-id))

      (fact "rejecting with a non-numeric todo id"
        (with-main "update" non-numeric-id) => (not-found-msg non-numeric-id))
      
      (fact "rejecting without a task"
        (with-main "update" 1 empty-task) => empty-task-msg)
      
      (fact "successfully with todo id passed as string"
        (with-main "update" "1" "updated") => truthy))

    (facts "adds a todo"

      (fact "successfully"
        (with-main "add" "fourth") => "Added: 4 fourth"  
        (list-todos) => ["1 first" "2 second" "3 third" "4 fourth"])    

      (fact "rejecting without a task"
        (with-main "add") => empty-task-msg
        (with-main "add" empty-task) => empty-task-msg))

    (facts "deletes a todo"

      (fact "successfully"
        (with-main "delete" 2) => "Deleted: 2 second"
        (list-todos) => ["1 first" "2 third"])

      (fact "rejecting with a nonexisting todo id"
        (with-main "delete" non-existing-id) => (not-found-msg non-existing-id))

      (fact "rejecting with a non-numeric todo id"
        (with-main "delete" non-numeric-id) => (not-found-msg non-numeric-id))
      
      (fact "rejecting without the todo id"
        (with-main "delete") => "No todo number given!")

      (fact "successfully with todo id passed as string"
        (with-main "delete" "1") => truthy))
    
    (fact "prints usage on unknown action"
      (with-main "unknown") => #"\s*Usage")))

