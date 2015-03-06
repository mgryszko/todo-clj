(ns todo.app.cli.validation)

(def ^{:private true} messages
  {:task_empty "Empty task!"
   :id_mandatory "No todo number given!"
   :id_not_found "No todo with number %s!"})

(defn print-message [key & args]
  (->> (apply format (into [(key messages)] args))
       (println)))

(defmacro proceed-if [validate-fn on-success]
  `(let [validation-result# ~validate-fn]
     (if (= (first validation-result#) :ok)
       ~on-success
       (apply print-message validation-result#))))
