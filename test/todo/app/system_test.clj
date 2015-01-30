(ns todo.app.system-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [midje.sweet :refer :all]
            [todo.app.main :refer [-main]]
            [todo.infrastructure.file.checkers :refer :all]
            [todo.infrastructure.file.test-operations :refer :all]))

(background (before :facts (delete-todo-file))
            (after :facts (delete-todo-file)))

(facts "todo application"
  (fact "adds and updates todos"
    (-main "add" "first") => (file-saved ["first"])
    (-main "add" "second") => (file-saved ["first", "second"]) 
    (-main "add" "third") => (file-saved ["first", "second", "third"]) 
    (-main "update" 1 "first updated") => (file-saved ["first updated", "second", "third"]) 
    (-main "update" 3 "third updated") => (file-saved ["first updated", "second", "third updated"]))

  (fact "new or updated todo without task is rejected"
    (let [expected-message #"Empty task!"]
      (with-out-str (-main "add")) => (every-checker expected-message file-not-created)
      (with-out-str (-main "add" "")) => (every-checker expected-message file-not-created)
      (with-out-str (-main "update" 1)) => (every-checker expected-message file-not-created)
      (with-out-str (-main "update" 1 " \t\n ")) => (every-checker expected-message file-not-created)))

  (fact "updated todo with nonexisting task number is rejected"
    (let [expected-message #"No task with number 1!"]
      (with-out-str (-main "update" 1 "task")) => (every-checker expected-message file-not-created)))
  
  (fact "prints usage on unknown action"
    (with-out-str (-main "unkown")) => #"\s+Usage"))
