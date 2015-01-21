(ns todo.infrastructure.file.test-operations
   (:require [clojure.java.io :as io]
             [clojure.string :as str]
             [todo.infrastructure.file.todo-repository :refer [file-name]]))

(defn delete-todo-file []
    (io/delete-file file-name true))

(defn get-todos []
  (str/split-lines (slurp file-name)))
