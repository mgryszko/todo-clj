(ns todo.infrastructure.file.checkers
  (:require [midje.sweet :refer [defchecker chatty-checker]]
            [todo.infrastructure.file.test-operations :refer :all]))

(defchecker file-saved [expected-todos]
    (chatty-checker [_]
                    (= (get-todos) expected-todos)))

