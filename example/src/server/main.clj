(ns cljsd.example.server.main
  (:require [cljsd.core :as cljsd]
            [ring.adapter.jetty :refer (run-jetty)]))

;;XXX hard coded paths
(def cljsd-config {:mount "/app/"
                   :src "/Users/brandon/Projects/cljsd/example/src/client/"
                   :dest "/Users/brandon/Projects/cljsd/example/public/js/"})

(def handler (-> (fn [request]
                   (prn (:uri request))
                   (when (= (:uri request) "/")
                     {:status 200
                      :headers {"Content-Type" "text/html"}
                      :body "Hello World"}))
                 (cljsd/wrap cljsd-config)))

(defonce jetty (atom nil))

(defn stop []
  (when-let [server @jetty]
    (.stop server)
    (reset! jetty nil)))

(defn start []
  (stop)
  (let [server (run-jetty #'handler {:port 3000 :daemon? true :join? false})]
    (reset! jetty server)
    server))

(defn main [& args]
  (.join (start)))
