(ns todo.infrastructure.file.operations
  (:require [clojure.java.io :refer [as-file reader writer]])
  (:import [java.nio.file Files CopyOption StandardCopyOption]))

(defn file-exists? [f] (.exists (as-file f)))

(defn count-lines [f]
  (if (file-exists? f)
    (with-open [r (reader f)]
      (count (line-seq r)))
    0))

(defn read-lines [f]
  (if (file-exists? f)
    (with-open [r (reader f)]
      (into [] (line-seq r)))
    []))

(defn write-lines [f lines]
  (with-open [w (writer f)]
     (doseq [line lines]
       (.write w line)
       (.newLine w))))

(defn atomic-move [src dest]
    (Files/move (.toPath (as-file src)) 
                (.toPath (as-file dest)) 
                (into-array CopyOption [StandardCopyOption/ATOMIC_MOVE])))

