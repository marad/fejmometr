(ns fejmetr.handler
  (:require [fejmetr.parser :as parser]
            [fejmetr.repo :as repo]
            [clojure.string :as s]))

; {
;     "color": "green",
;     "message": "It's going to be sunny tomorrow! (yey)",
;     "notify": false,
;     "message_format": "text"
; }

(defn add [message args]
  (let [[taker amount reason] (s/split args #"\s" 3)]
    (repo/add-fame taker "from" reason (read-string amount))
    {:color "green"
     :message (str taker " got " amount " fame; reason: " reason)}))

(defn show [message user]
  {:color "green"
   :message (str user " has " (repo/get-fame user) " fame")
   })

(defn leaders [message args]
  {:color "green"
   :message (->> (repo/leaders 5)
                 (map #(str (get % 0) ": " (get % 1)))
                 (s/join "\n")
                 )})

(def handlers
  {"add" add
   "show" show
   "leaders" leaders})

(defn dispatch
  "Parses and dispatches message to apropriate command"
  [message]
  (let [{:keys [name args]} (parser/parse-command
                              (get-in message [:item :message :message]))]
    (if-let [handler (handlers name)]
      (handler message args)
      {:color "red"
       :message "Invalid command!"}
      )))
