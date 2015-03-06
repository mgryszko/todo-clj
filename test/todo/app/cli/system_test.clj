(ns todo.app.cli.system-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [midje.sweet :refer :all]
            [todo.app.cli.main :refer [-main]]
            [todo.infrastructure.file.checkers :refer :all]
            [todo.infrastructure.file.test-operations :refer :all]))

(defmacro with-main [command & args]
  `(str/trim (with-out-str (-main ~command ~@args))))

(defn add-silently [& tasks]
  (doall (map #(with-main "add" %) tasks)))

(background (before :facts (delete-todo-file))
            (after :facts (delete-todo-file)))

(facts "todo application"
  (fact "adds and updates todos"
    (with-main "add" "first")
      => (every-checker "Added: 1 first" (file-saved ["first"]))
    (with-main "add" "second") =>
       (every-checker "Added: 2 second" (file-saved ["first" "second"]))
    (with-main "add" "third") =>
      (every-checker "Added: 3 third" (file-saved ["first" "second" "third"]))
    (with-main "update" 1 "first updated") =>
      (every-checker "Updated: 1 first\n     to: 1 first updated" 
                     (file-saved ["first updated" "second" "third"]))
    (with-main "update" 3 "third updated") =>
      (every-checker "Updated: 3 third\n     to: 3 third updated" 
                     (file-saved ["first updated" "second" "third updated"])))

  (fact "new todo without task is rejected"
    (let [expected-message "Empty task!"]
      (with-main "add") => (every-checker expected-message file-not-created)
      (with-main "add" "") => (every-checker expected-message file-not-created)))

  (against-background [(before :facts (add-silently "first"))]
    (fact "updated todo with nonexisting todo number is rejected"
      (with-main "update" 2) => "No todo with number 2!")

    (fact "updated todo with non-numeric todo number is rejected"
      (with-main "update" "two") => "No todo with number two!")
    
    (fact "updated todo without task is rejected"
      (with-main "update" 1 " \t\n ") => "Empty task!")
    
    (fact "updated todo number can be passed as string"
      (with-main "update" "1" "updated") => truthy))

  (against-background [(before :facts (add-silently "first" "second" "third"))]
    (fact "deletes a todo"
      (with-main "delete" 2) => (file-saved ["first" "third"]))

    (fact "deletion of a nonexisting todo number is rejected"
      (with-main "delete" 4) => "No todo with number 4!")

    (fact "delete of todo with non-numeric todo number is rejected"
      (with-main "delete" "two") => "No todo with number two!")
    
    (fact "deletion of a todo without the number is rejected"
      (with-main "delete") => "No todo number given!")

    (fact "deleted todo number can be passed as string"
      (with-main "delete" "1") => truthy))

  (against-background [(before :facts (add-silently "first" "second" "third"))]
    (fact "lists todos"
      (with-main "list") => "1 first\n2 second\n3 third"))
  
  (fact "prints usage on unknown action"
    (with-main "unknown") => #"\s*Usage"))
