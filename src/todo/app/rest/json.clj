(ns todo.app.rest.json
  (:require [clojure.data.json :as json]))

(defn- body-as-string [{{:keys [body]} :request}]
  (slurp (clojure.java.io/reader body)))

(defn body-as-json [ctx]
  (-> (body-as-string ctx)
      (json/read-str :key-fn keyword)))
