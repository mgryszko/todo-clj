(ns todo.core-test
  (:require [midje.sweet :refer :all]
            [todo.core :refer :all]))

(declare save-todo!)

(facts "about todo use cases"
  (let [any-new-task {:task "any task"}
        any-persistent-todo (conj {:id 1} any-new-task)]
    (facts "about adding todo"
      (fact "empty task cannot be added"
        (can-todo-be-added? "any task") => :ok
        (can-todo-be-added? "a") => :ok
        (can-todo-be-added? "") => :task_empty
        (can-todo-be-added? " \t\n\r ") => :task_empty)
      (fact "if still tried to be added, an exception is thrown"
        (add-todo #() "") => (throws IllegalArgumentException))
      (fact "non-empty task is added"
        (add-todo save-todo! "any task") => any-persistent-todo
          (provided
            (save-todo! any-new-task) => any-persistent-todo :times 1)))

     (fact "todo is updated"
      (update-todo save-todo! any-persistent-todo) => any-persistent-todo
        (provided
          (save-todo! any-persistent-todo) => any-persistent-todo :times 1))))

