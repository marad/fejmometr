(ns fejmetr.handler
  (:require [fejmetr.parser :as parser]
            [fejmetr.repo :as repo]
            [fejmetr.command.add :as add]
            [clojure.string :as s]
            ))

; {:event room_message, :item {:message {:date 2016-09-04T07:00:40.750138+00:00, :from {:id 1287205, :links {:self https://api.hipchat.com/v2/user/1287205}, :mention_name MarcinRadoszewski, :name Marcin Radoszewski, :version 00000000}, :id 0c96f06f-a945-4fe7-97f3-9644ae803638, :mentions [{:id 1287205, :links {:self https://api.hipchat.com/v2/user/1287205}, :mention_name MarcinRadoszewski, :name Marcin Radoszewski, :version 00000000}], :message /fame add @MarcinRadoszewski 10 xxx, :type message}, :room {:id 2357032, :is_archived false, :links {:members https://api.hipchat.com/v2/room/2357032/member, :participants https://api.hipchat.com/v2/room/2357032/participant, :self https://api.hipchat.com/v2/room/2357032, :webhooks https://api.hipchat.com/v2/room/2357032/webhook}, :name mori-test, :privacy private, :version W8TZBO25}}, :oauth_client_id 515ca846-c394-41eb-a9e0-bbe703e9e6f5, :webhook_id 6011215}

; {
;     "color": "green",
;     "message": "It's going to be sunny tomorrow! (yey)",
;     "notify": false,
;     "message_format": "text"
; }

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
  {"add" add/execute
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
