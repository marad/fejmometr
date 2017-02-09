(ns fejmetr.command.add
  (:require [clojure.string :as str]
            [fejmetr.repo :as repo]
            [fejmetr.message :as msg]
            [schema.core :as s]
            ))

(def AddArgs {:receiver s/Str
              :amount s/Num
              :reason s/Str})
(def HCResponse {:color s/Str
                 :message s/Str})

(s/defn parse-args :- AddArgs
  [message :- msg/Message]
  (let [args (msg/command-args message)
        [receiver amount reason] (str/split args #"\s+" 3)]
  {:receiver (msg/clear-mention receiver)
   :amount (read-string amount)
   :reason reason}))

(s/defn decorate :- HCResponse
  [receiver :- msg/Mention
   response :- HCResponse]
  (if (= (:name receiver) "Dorota Leszczynska")
    (assoc response
           :color "purple"
           :message (str "(heart) " (:message response) " (heart)")
           :message_format "text"
           :notify false
           )
    response
    )
  )

(s/defn execute :- HCResponse
  [message :- msg/Message]
  (let [args (parse-args message)
        sender (msg/sender message)
        receiver (msg/find-mention message (:receiver args))
        ]
    (cond
      (= (:id sender) (:id receiver)) {:color "red"
                                       :message "That's cheating!"
                                       }
      (nil? receiver) {:color "red"
                       :message (str "Who is " (:receiver args) "?")
                       }
      ((comp not pos?) (:amount args)) {:color "red"
                                        :message (str "That's not adding fame...")
                                        }
      :else (do (repo/add-fame (:name receiver)
                               (:name sender)
                               (:reason args)
                               (:amount args)
                               (. System currentTimeMillis))
                (decorate
                  receiver
                  {:color "green"
                   :message (str (:mention_name receiver) " got " (:amount args) " fame from "
                                 (:mention_name sender) "; reason: " (:reason args))}))
      )))
