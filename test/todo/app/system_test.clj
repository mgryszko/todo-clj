(ns todo.app.system-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [midje.sweet :refer :all]
            [todo.app.main :refer [-main]]
            [todo.infrastructure.file.checkers :refer :all]
            [todo.infrastructure.file.test-operations :refer :all]))

(defmacro with-trimmed-out-str [body]
  `(str/trim (with-out-str ~body)))

(background (before :facts (delete-todo-file))
            (after :facts (delete-todo-file)))

(defn add-silently [& tasks]
  (doall (map #(with-out-str (-main "add" %)) tasks)))

(facts "todo application"
  (fact "adds and updates todos"
    (with-trimmed-out-str (-main "add" "first"))
      => (every-checker "Added: 1 first" (file-saved ["first"]))
    (with-trimmed-out-str (-main "add" "second")) =>
       (every-checker "Added: 2 second" (file-saved ["first" "second"]))
    (with-trimmed-out-str (-main "add" "third")) =>
      (every-checker "Added: 3 third" (file-saved ["first" "second" "third"]))
    (-main "update" 1 "first updated") => (file-saved ["first updated" "second" "third"])
    (-main "update" 3 "third updated") => (file-saved ["first updated" "second" "third updated"]))

  (fact "new todo without task is rejected"
    (let [expected-message "Empty task!"]
      (with-trimmed-out-str (-main "add")) => (every-checker expected-message file-not-created)
      (with-trimmed-out-str (-main "add" "")) => (every-checker expected-message file-not-created)))

  (against-background [(before :facts (add-silently "first"))]
    (fact "updated todo with nonexisting task number is rejected"
      (let [expected-message "No task with number 2!"]
        (with-trimmed-out-str (-main "update" 2)) => expected-message))

    (fact "updated todo without task is rejected"
      (let [expected-message "Empty task!"]
        (with-trimmed-out-str (-main "update" 1 " \t\n ")) => expected-message)))

  (against-background [(before :facts (add-silently "first" "second" "third"))]
    (fact "lists todos"
      (with-trimmed-out-str (-main "list")) => "1 first\n2 second\n3 third"))

  (fact "prints usage on unknown action"
    (with-out-str (-main "unkown")) => #"\s+Usage"))
