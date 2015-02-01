(ns todo.app.validation)

(def ^{:private true} messages
  {:task_empty "Empty task!"
   :id_not_found "No task with number %s!"})

(defn print-message [key & args]
  (println (apply format (into [(key messages)] args))))

(defmacro proceed-if [validate-fn on-success]
  `(let [validation-result# ~validate-fn]
     (if (= (first validation-result#) :ok)
       ~on-success
       (apply print-message validation-result#))))
