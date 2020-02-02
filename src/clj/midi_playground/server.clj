(ns midi-playground.server
  (:require [midi-playground.handler :refer [handler]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

 (defn -main [& _args]
   (let [port (Integer/parseInt (or (env :port) "3000"))]
     (run-jetty handler {:port port :join? false})))
