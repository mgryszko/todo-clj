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

  (fact "new todo without task is rejected"
    (let [expected-message #"Empty task!"]
      (with-out-str (-main "add")) => (every-checker expected-message file-not-created)
      (with-out-str (-main "add" "")) => (every-checker expected-message file-not-created)))

  (against-background [(before :facts (-main "add" "first"))]
    (fact "updated todo with nonexisting task number is rejected"
      (let [expected-message #"No task with number 2!"]
        (with-out-str (-main "update" 2)) => expected-message))

    (fact "updated todo without task is rejected"
      (let [expected-message #"Empty task!"]
        (with-out-str (-main "update" 1 " \t\n ")) => expected-message)))

  (against-background [(before :facts ((-main "add" "first")
                                       (-main "add" "second")
                                       (-main "add" "third")))]
    (fact "lists todos"
      (with-out-str (-main "list")) => "1 first\n2 second\n3 third\n"))

  (fact "prints usage on unknown action"
    (with-out-str (-main "unkown")) => #"\s+Usage"))
