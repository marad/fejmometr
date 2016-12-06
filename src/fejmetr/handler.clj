(ns fejmetr.handler
  (:require [fejmetr.repo :as repo]
            [fejmetr.command.add :as add]
            [fejmetr.message :as msg]
            [clojure.string :as s]
            ))

(defn show [message]
  (let [user (msg/find-mention message (msg/command-args message))]
    {:color "green"
     :message (str (:mention_name user) " has " (repo/get-fame (:name user)) " fame")
     }))

(defn- apply-max-fame [[user-name fame]]
  (if (= user-name "Dorota Leszczynska")
    [user-name (str "MAX (" fame ")")]
    [user-name fame]
    ))

(defn leaders [message]
  {:color "green"
   :message_format "html"
   :message (->> (repo/leaders 100)
                 (map apply-max-fame)
                 (map #(str (get % 0) ": " (get % 1)))
                 (s/join "<br/>")
                 )})

(defn help [message]
  {:color "green"
   :notify false
   :message_format "html"
   :message "<b>Format: /fame command [args]</b><br/>
            Commands:<br />
            <ul>
             <li><b>add</b> <i>person amount reason</i> - adds fame!</li>
             <li><b>show</b> <i>person</i> - shows fame</li>
             <li><b>leaders</b> - show leaders</li>
            </ul>
            "
   })

(def handlers
  {"add" add/execute
   "show" show
   "leaders" leaders
   "help" help
   })

(defn dispatch
  "Parses and dispatches message to apropriate command"
  [message]
  (if-let [handler (handlers (msg/command message))]
    (handler message)
    {:color "red"
     :message "Invalid command!"}))
