(ns todo.infrastructure.file.repository
  (:require [todo.infrastructure.file.operations :refer :all])
  (:import [java.io File]))

(def file-name "todo.txt")

(defn- modify-nth-line [lines todo]
  (let [line-number (:id todo)] 
     (assoc lines (- line-number 1) (:task todo))))

(defn update-todo! [todo]
  (let [lines (modify-nth-line (read-lines file-name) todo)
        temp-file (File/createTempFile "todo" nil)]
    (write-lines temp-file lines)
    (atomic-move temp-file file-name))
  todo)

(defn- next-id []
  (+ (count-lines file-name) 1))

(defn add-todo! [todo]
  (->> (assoc todo :id (next-id))
       (update-todo!)))

(defn id-exists? [id]
  (and (>= id 1) (<= id (count-lines file-name))))

(defn find-all []
  (let [lines (read-lines file-name)
        ids (iterate inc 1)]
    (->> (map vector ids lines)
         (map #(hash-map :id (first %) :task (second %)))
         (into []))))

(defn find-by-line-number [line-num]
  (get (into [] (find-all)) (- line-num 1)))

