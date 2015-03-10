(ns todo.app.rest.server
  (:require [ring.server.standalone :as server]
            [todo.app.rest.handler :refer [handler]]))
(defonce server (atom nil))

(defn start-server [port]
  (reset! server
    (server/serve handler {:port port :open-browser? false :auto-reload? false})))

(defn stop-server []
  (.stop @server)
  (reset! server nil))
