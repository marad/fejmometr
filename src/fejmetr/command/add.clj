(ns fejmetr.command.add
  (:require [clojure.string :as s]
            [fejmetr.repo :as repo]
            [fejmetr.message :as msg]
            ))

(defn parse-args [message]
  (let [args (msg/command-args message)
        [receiver amount reason] (s/split args #"\s" 3)]
  {:receiver (msg/clear-mention receiver)
   :amount (read-string amount)
   :reason reason}))

(defn execute [message]
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
      :else (do (repo/add-fame (:name receiver) (:name sender) (:reason args) (:amount args))
                {:color "green"
                 :message (str (:mention_name receiver) " got " (:amount args) " fame from "
                               (:mention_name sender) "; reason: " (:reason args))})
      )))
