(ns todo.core-test
  (:require [midje.sweet :refer :all]
            [todo.core :refer :all]))

(declare save-todo!)
(declare update-todo!)

(facts "todo use cases"
  (let [any-new-task {:task "any task"}
        any-persistent-todo (conj {:id 1} any-new-task)] 

    (fact "todo is created"
      (create-todo save-todo! "any task") => any-persistent-todo
        (provided
          (save-todo! any-new-task) => any-persistent-todo :times 1))

     (fact "todo is updated"
      (update-todo update-todo! any-persistent-todo) => any-persistent-todo
        (provided
          (update-todo! any-persistent-todo) => any-persistent-todo :times 1))))

