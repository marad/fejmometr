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
    {:message (str taker " got " amount " fame; reason: " reason)}))

(defn leaders [message args])

(def handlers
  {"add" add
   "leaders" leaders})

(defn dispatch
  "Parses and dispatches message to apropriate command"
  [message]
  (let [{:keys [name args]} (parser/parse-command
                              (get-in message [:item :message :message]))]
    ((handlers name) message args)))
