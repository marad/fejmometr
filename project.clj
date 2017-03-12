(defproject fejmetr "1.0.1-SNAPSHOT"
  :description "Demo Clojure web app"
  :url "http://fejmetr.herokuapp.com"
  :license {:name "Eclipse Public License v1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/clojurescript "1.9.293"]
                 [prismatic/schema "1.1.3"]
                 [compojure "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [org.clojure/data.json "0.2.6"]
                 [clj-time "0.12.0"]
                 [environ "1.0.0"]
                 [com.novemberain/monger "3.1.0"]
                 [mount "0.1.10"]

                 ;; CLJS dependencies
                 [reagent "0.6.0"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.3.1"]
            [lein-cljsbuild "1.1.4"]
            [lein-figwheel "0.5.4-7"]
            [lein-embongo "0.2.2"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "fejmetr.jar"
  :aot [fejmetr.web]
  :main fejmetr.web
  :profiles {:production {:env {:production true}}}

  ;; CLJS CONFIGS
  :cljsbuild {:builds [{:id "main"
                        :source-paths ["front/"]
                        :figwheel true
                        :compiler {:output-to "resources/public/js/main.js"
                                   :output-dir "resources/public/js/out"
                                   :optimizations :none
                                   :pretty-print true}}]}
  :figwheel {:http-server-root "public"
             :server-port 5309
             :server-ip "0.0.0.0"

             :css-dirs ["resources/public/css"]
             :ring-handler fejmetr.web/app})
