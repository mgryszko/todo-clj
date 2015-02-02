(ns todo.core-test
  (:require [midje.sweet :refer :all]
            [todo.core :refer :all]))

(declare save-todo!)
(declare id-exists?)
(declare find-all)

(facts "about todo use cases"
  (let [any-task "any task"
        any-short-task "a"
        empty-task ""
        blank-task " \t\n\r "
        existing-id 1
        any-persistent-todo {:id 1 :task any-task}
        non-existing-id 2]
    (facts "about adding todo"
      (fact "empty task cannot be added"
        (can-todo-be-added? any-task) => [:ok]
        (can-todo-be-added? any-short-task) => [:ok]
        (can-todo-be-added? empty-task) => [:task_empty]
        (can-todo-be-added? blank-task) => [:task_empty])

      (fact "if still tried to be added, an exception is thrown"
        (add-todo save-todo! empty-task) => (throws IllegalArgumentException))

      (fact "non-empty task is added"
        (add-todo save-todo! any-task) => any-persistent-todo
          (provided
            (save-todo! {:task any-task}) => any-persistent-todo :times 1)))

    (facts "about updating todo"
      (fact "empty task cannot be updated"
        (against-background (id-exists? existing-id) => true)
        (can-todo-be-updated? id-exists? any-persistent-todo) => [:ok]
        (can-todo-be-updated? id-exists? (assoc-in any-persistent-todo [:task] any-short-task)) => [:ok]
        (can-todo-be-updated? id-exists? (assoc-in any-persistent-todo [:task] empty-task)) => [:task_empty]
        (can-todo-be-updated? id-exists? (assoc-in any-persistent-todo [:task] blank-task)) => [:task_empty])

      (fact "non-existing task id cannot be updated"
         (can-todo-be-updated? id-exists? {:id non-existing-id :task any-task}) => [:id_not_found non-existing-id]
           (provided (id-exists? non-existing-id) => false))

      (fact "if still tried to be updated, an exception is thrown"
         (against-background (id-exists? existing-id) => false)
         (update-todo id-exists? save-todo! any-persistent-todo) => (throws IllegalArgumentException))

      (fact "todo is updated"
        (against-background (id-exists? (any-persistent-todo :id)) => true)
        (update-todo id-exists? save-todo! any-persistent-todo) => any-persistent-todo
          (provided
            (save-todo! any-persistent-todo) => any-persistent-todo :times 1)))

    (facts "about finding all todos"
      (fact "all todos are listed"
        (find-all-todos find-all) => [any-persistent-todo]
          (provided (find-all) => [any-persistent-todo])))))


