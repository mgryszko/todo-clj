(ns todo.infrastructure.file.repository
  (:require [todo.infrastructure.file.operations :refer :all])
  (:import [java.io File]))

(def file-name "todo.txt")

(defn next-id []
  (+ (if (file-exists file-name) (count-lines file-name) 0) 1))

(defn modify-nth-line [lines todo]
  (let [line-number (:id todo)] 
     (assoc lines (- line-number 1) (:task todo))))

(defn update-todo! [todo]
  (let [line-number (:id todo)
        lines (modify-nth-line (read-lines file-name) todo)
        temp-file (File/createTempFile "todo" nil)] 
    (write-lines temp-file lines)
    (atomic-move temp-file file-name))
  todo)

(defn add-todo! [todo] 
  (update-todo! (assoc todo :id (next-id))))

