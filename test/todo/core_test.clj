(ns todo.core-test
  (:require [midje.sweet :refer :all]
            [todo.core :refer :all]))

(fact "failing"
  (hello) => "hello")
