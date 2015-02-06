(ns todo.infrastructure.file.repository-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [midje.sweet :refer :all]
            [todo.infrastructure.file.checkers :refer :all]
            [todo.infrastructure.file.repository :refer :all]
            [todo.infrastructure.file.test-operations :refer :all]))

(background (before :facts (delete-todo-file))
            (after :facts (delete-todo-file)))

(facts "todo file repository"
  (fact "adds single todo"
    (add-todo! {:task "any"}) =>
      (every-checker (file-saved ["any"])
                     {:id 1 :task "any"}))

  (fact "adds two todos"
    (add-todo! {:task "first"})
    (add-todo! {:task "second"}) =>
      (every-checker (file-saved ["first", "second"])
                                 {:id 2 :task "second"}))
  (against-background 
    [(before :facts ((add-todo! {:task "first"})
                     (add-todo! {:task "second"})
                     (add-todo! {:task "third"})))]
      
    (fact "updates first todo"
      (let [todo {:id 1 :task "updated"}]
        (update-todo! todo) =>
          (every-checker (file-saved ["updated" "second" "third"])
                         {:id 1 :task "updated"})))

    (fact "updates last todo"
      (let [todo {:id 3 :task "updated"}]
        (update-todo! todo) =>
          (every-checker (file-saved ["first" "second" "updated"])
                         {:id 3 :task "updated"})))

    (fact "finds all todos"
      (find-all) => [{:id 1 :task "first"} {:id 2 :task "second"} {:id 3 :task "third"}])

    (fact "finds todo by line number"
      (find-by-line-number 1) => {:id 1 :task "first"}
      (find-by-line-number 3) => {:id 3 :task "third"})))
