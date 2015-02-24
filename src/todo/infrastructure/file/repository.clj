(ns todo.infrastructure.file.repository
  (:require [todo.infrastructure.file.operations :refer :all]
            [environ.core :refer [env]])
  (:import [java.io File]))

(def file-name (or (env :todo-file) "todo.txt"))

(defn- modify-nth-line [lines todo]
  (let [line-num (:id todo)] 
     (assoc lines (- line-num 1) (:task todo))))

(defn- delete-nth-line [lines line-num]
  (into [] (concat
             (subvec lines 0 (- line-num 1))
             (subvec lines line-num))))

(defn- make-todo [line-num line] {:id line-num :task line})

(defn update-todo! [todo]
  (let [lines (modify-nth-line (read-lines file-name) todo)
        temp-file (File/createTempFile "todo" nil)]
    (write-lines temp-file lines)
    (atomic-move temp-file file-name))
  todo)

(defn add-todo! [todo]
  (->> (assoc todo :id (next-line-num file-name))
       (update-todo!)))

(defn delete-todo! [line-num]
  (let [lines (read-lines file-name)
        truncated-lines (delete-nth-line lines line-num)
        temp-file (File/createTempFile "todo" nil)]
    (write-lines temp-file truncated-lines)
    (atomic-move temp-file file-name)
    (make-todo line-num (get lines (- line-num 1)))))

(defn line-num-exists? [line-num]
  (and (number? line-num) (>= line-num 1) (<= line-num (count-lines file-name))))

(defn find-all []
  (let [lines (read-lines file-name)
        line-nums (iterate inc 1)]
    (->> (map vector line-nums lines)
         (map #(apply make-todo %))
         (into []))))

(defn find-by-line-number [line-num]
  (get (into [] (find-all)) (- line-num 1)))

