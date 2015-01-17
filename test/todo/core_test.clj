(ns todo.core-test
  (:require [midje.sweet :refer :all]
            [todo.core :refer :all]))

(declare save-todo!)

(fact "todo is created"
  (create-todo save-todo! "any task") => {:id 1 :task "any task"}
    (provided
      (save-todo! {:task "any task"}) => {:id 1 :task "any task"} :times 1))

