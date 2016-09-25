(defproject fejmetr "1.0.0-SNAPSHOT"
  :description "Demo Clojure web app"
  :url "http://fejmetr.herokuapp.com"
  :license {:name "Eclipse Public License v1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [org.clojure/data.json "0.2.6"]
                 [clj-time "0.12.0"]
                 [environ "1.0.0"]
                 [com.novemberain/monger "3.1.0"]
                 [mount "0.1.10"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.3.1"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "fejmetr.jar"
  :profiles {:production {:env {:production true}}})
