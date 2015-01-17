(ns todo.infrastructure.file.todo-repository
  (require [clojure.java.io :as io]))

(def file-name "todo.txt")

(def line-separator (System/getProperty "line.separator"))

(defn file-exists [] (.exists (io/as-file file-name)))

(defn count-lines []
  (with-open [rdr (io/reader file-name)]
             (count (line-seq rdr))))

(defn last-id []
  (if (file-exists) (count-lines) 0))

(defn save-todo! [todo] 
  (spit "todo.txt" (str (:task todo) line-separator) :append true)
        (conj todo {:id (last-id)}))
