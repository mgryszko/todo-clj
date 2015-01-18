(ns todo.infrastructure.file.todo-repository-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [midje.sweet :refer :all]
            [todo.infrastructure.file.todo-repository :refer :all]))

(defn delete-todo-file []
    (io/delete-file "todo.txt" true))

(defn get-lines [file]
  (str/split-lines (slurp file)))

(defchecker file-saved [expected-todos]
    (chatty-checker [_]
                    (= (get-lines "todo.txt") expected-todos)))

(background (before :facts (delete-todo-file))
            (after :facts (delete-todo-file)))

(facts "todo file repository"
  (fact "saves single todo"
    (save-todo! {:task "any"}) =>
      (every-checker (file-saved ["any"])
                     {:id 1 :task "any"}))

  (fact "saves two todos"
    (save-todo! {:task "first"})
    (save-todo! {:task "second"}) =>
      (every-checker (file-saved ["first", "second"])
                                 {:id 2 :task "second"}))
  (against-background 
    [(before :facts ((save-todo! {:task "first"})
                     (save-todo! {:task "second"})
                     (save-todo! {:task "third"})))]
      
    (fact "updates first todo"
      (let [todo {:id 1 :task "updated"}]
        (update-todo! todo) =>
          (every-checker (file-saved ["updated" "second" "third"])
                         {:id 1 :task "updated"})))

    (fact "updates last todo"
      (let [todo {:id 3 :task "updated"}]
        (update-todo! todo) =>
          (every-checker (file-saved ["first" "second" "updated"])
                         {:id 3 :task "updated"})))))
