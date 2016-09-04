(ns fejmetr.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [clojure.data.json :as json]
            [fejmetr.handler :as handler]
            [environ.core :refer [env]]))

(defn splash []
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello from Heroku!"})

(defroutes app
  (GET "/" []
       (splash))
  (POST "/command"
        {body :body}
        (let [data (json/read-str (slurp body) :key-fn keyword)]
          (println data)
          {:status 200
           :headers {"Content-Type" "application/json"}
           :body (json/write-str (handler/dispatch data))}))

  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
