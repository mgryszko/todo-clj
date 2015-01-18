(ns todo.infrastructure.file.todo-repository
  (require [clojure.java.io :as io])
  (import [java.io RandomAccessFile]))

(def file-name "todo.txt")

(def line-separator (System/getProperty "line.separator"))

(defn file-exists [] (.exists (io/as-file file-name)))

(defn count-lines []
  (with-open [r (io/reader file-name)]
    (count (line-seq r))))

(defn read-all-lines []
  (if (file-exists)
    (with-open [rdr (io/reader file-name)]
      (into [] (line-seq rdr)))
    []))

(defn next-id []
  (+ (if (file-exists) (count-lines) 0) 1))

(defn modify-nth-line [lines todo]
  (let [line-number (:id todo)] 
     (assoc lines (- line-number 1) (:task todo))))

(defn update-todo! [todo]
  (let [line-number (:id todo)
        lines (modify-nth-line (read-all-lines) todo)] 
    (with-open [w (io/writer file-name)]
       (doseq [line lines]
         (.write w line)
         (.newLine w))))
  todo)

(defn add-todo! [todo] 
  (update-todo! (assoc todo :id (next-id))))

