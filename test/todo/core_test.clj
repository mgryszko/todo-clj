(ns todo.core-test
  (:require [midje.sweet :refer :all]
            [todo.core :refer :all]))

(declare save-todo! delete-todo! id-exists? find-todo)

(def any-task "any task")
(def any-short-task "a")
(def empty-task "")
(def blank-task " \t\n\r ")
(def existing-id 1)
(def any-persistent-todo {:id 1 :task any-task})
(def non-existing-id 2)

(facts "todo use cases"

  (facts "add todo"

    (fact "empty todo cannot be added"
      (can-todo-be-added? any-task) => [:ok]
      (can-todo-be-added? any-short-task) => [:ok]
      (can-todo-be-added? empty-task) => [:task-empty]
      (can-todo-be-added? blank-task) => [:task-empty])

    (fact "if still tried to be added, an exception is thrown"
      (add-todo save-todo! empty-task) => (throws IllegalArgumentException))

    (fact "non-empty todo is added"
      (add-todo save-todo! any-task) => any-persistent-todo
        (provided (save-todo! {:task any-task}) => any-persistent-todo :times 1)))

  (facts "update todo"

    (fact "todo is updated"
      (against-background (id-exists? (any-persistent-todo :id)) => true)
      (update-todo id-exists? save-todo! any-persistent-todo) => any-persistent-todo
        (provided (save-todo! any-persistent-todo) => any-persistent-todo :times 1))

    (fact "empty todo cannot be updated"
      (against-background (id-exists? existing-id) => true)
      (can-todo-be-updated? id-exists? any-persistent-todo) => [:ok]
      (can-todo-be-updated? id-exists? (assoc-in any-persistent-todo [:task] any-short-task)) => [:ok]
      (can-todo-be-updated? id-exists? (assoc-in any-persistent-todo [:task] empty-task)) => [:task-empty]
      (can-todo-be-updated? id-exists? (assoc-in any-persistent-todo [:task] blank-task)) => [:task-empty])

    (fact "non-existing todo id cannot be updated"
       (can-todo-be-updated? id-exists? {:id non-existing-id :task any-task}) => [:id-not-found non-existing-id]
         (provided (id-exists? non-existing-id) => false))

    (fact "if still tried to be updated, an exception is thrown"
       (against-background (id-exists? non-existing-id) => false)
       (update-todo id-exists? save-todo! {:id non-existing-id :task any-task}) =>
         (throws IllegalArgumentException)))

  (facts "delete todo"

    (fact "todo is deleted"
      (delete-todo id-exists? delete-todo! existing-id) => any-persistent-todo
        (provided
          (id-exists? existing-id) => true
          (delete-todo! existing-id) => any-persistent-todo :times 1))
    
    (fact "todo without an id cannot be deleted"
      (can-todo-be-deleted? id-exists? nil) => [:id-mandatory]
      (can-todo-be-deleted? id-exists? false) => [:id-mandatory])

    (fact "non-existing todo id cannot be deleted"
      (can-todo-be-deleted? id-exists? non-existing-id) => [:id-not-found non-existing-id]
        (provided (id-exists? non-existing-id) => false))

    (fact "if still tried to be deleted, an exception is thrown"
       (against-background (id-exists? non-existing-id) => false)
       (delete-todo id-exists? delete-todo! non-existing-id) => (throws IllegalArgumentException)))

  (facts "finds todos"
         
    (fact "all todos"
      (find-all-todos find-todo) => [any-persistent-todo]
        (provided (find-todo) => [any-persistent-todo]))

    (fact "todo by id"
      (let [id (:id any-persistent-todo)]
      (find-todo-by-id find-todo id) => any-persistent-todo
        (provided (find-todo id) => any-persistent-todo)))))

