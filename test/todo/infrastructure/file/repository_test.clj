(ns todo.infrastructure.file.repository-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [midje.sweet :refer :all]
            [todo.infrastructure.file.checkers :refer :all]
            [todo.infrastructure.file.repository :refer :all]
            [todo.infrastructure.file.test-operations :refer :all]))

(background (before :facts (delete-todo-file)
                    :after (delete-todo-file)))

(facts "file repository"

  (facts "adds todo"
    (fact "single todo"
      (add-todo! {:task "any"}) =>
        (every-checker (file-saved ["any"])
                       {:id 1 :task "any"}))

    (fact "two todos"
      (add-todo! {:task "first"})
      (add-todo! {:task "second"}) =>
        (every-checker (file-saved ["first", "second"])
                                 {:id 2 :task "second"})))

  (against-background 
    [(before :facts [(add-todo! {:task "first"})
                     (add-todo! {:task "second"})
                     (add-todo! {:task "third"})
                     (add-todo! {:task "fourth"})])]
    (facts "updates todo"

      (fact "first todo"
        (let [todo {:id 1 :task "updated"}]
          (update-todo! todo) =>
            (every-checker (file-saved ["updated" "second" "third" "fourth"]) todo)))

      (fact "last todo"
        (let [todo {:id 4 :task "updated"}]
          (update-todo! todo) =>
            (every-checker (file-saved ["first" "second" "third" "updated"]) todo))))

    (facts "deletes todo"

      (fact "first todo"
        (let [line-num 1]
          (delete-todo! line-num) =>
            (every-checker (file-saved ["second" "third" "fourth"])
                           {:id line-num :task "first"})))

      (fact "in-between todo"
        (let [line-num 2]
          (delete-todo! line-num) =>
            (every-checker (file-saved ["first" "third" "fourth"])
                           {:id line-num :task "second"})))
      
      (fact "last todo"
        (let [line-num 4]
          (delete-todo! line-num) =>
            (every-checker (file-saved ["first" "second" "third"])
                           {:id line-num :task "fourth"})))) 
    (facts "finds todos"

      (fact "all"
        (find-all) => [{:id 1 :task "first"}
                       {:id 2 :task "second"}
                       {:id 3 :task "third"}
                       {:id 4 :task "fourth"}])

      (fact "by line number"
        (find-by-line-number 1) => {:id 1 :task "first"}
        (find-by-line-number 3) => {:id 3 :task "third"}
        (find-by-line-number 4) => {:id 4 :task "fourth"}))

    (fact "checks if a line number exists"
      (line-num-exists? 0) => false
      (line-num-exists? 1) => true
      (line-num-exists? 4) => true
      (line-num-exists? 5) => false
      (line-num-exists? "1") => false
      (line-num-exists? "one") => false)))
