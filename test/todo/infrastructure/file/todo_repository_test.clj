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
    (save-todo! {:task "any task"}) => (every-checker (file-saved ["any task"])
                                                      {:id 1 :task "any task"}))
  (fact "saves two todos"
    (save-todo! {:task "first task"})
    (save-todo! {:task "second task"}) => (every-checker (file-saved ["first task", "second task"])
                                                         {:id 2 :task "second task"})))
