(ns fejmetr.handler
  (:require [fejmetr.repo :as repo]
            [fejmetr.command.add :as add]
            [fejmetr.message :as msg]
            [fejmetr.error :as e]
            [clojure.string :as s]
            [rail.core :as r]
            ))

(defn- apply-max-fame [[user-name fame]]
  (if (= user-name "Dorota Leszczynska")
    [user-name (str "MAX (" fame ")")]
    [user-name fame]
    ))

(defn- format-leaders [leaders]
  (->> leaders
       (map apply-max-fame)
       (map #(str (get % 0) ": " (get % 1)))
       (s/join "<br/>")))

(defn- ->response [message]
  {:color "green"
   :message_format "html"
   :message message})

(defn- ->error-response [message]
  {:color "red"
   :message message})

(defn- user-error-message [message]
  (if-let [{error :type :as data} (ex-data message)]
          (case error
            :invalid-command "Invalid command!"
            :command-not-specified "Command not provided!"
            :arguments-not-specified "Missing command arguments!"
            :mention-not-found (str "Who is " (:mention data) "?")
            :missing-sender "Invalid message. Missing sender!"
            :cannot-add-fame-to-self "That's cheating!"
            :receivers-cannot-be-empty "Who should get the fame again?"
            :amount-not-provided "How much fame to add?"
            :amount-not-positive "That's not adding fame!"
            :not-connected-to-db "I'm not connected to database :("
            :user-not-found "User record was not found :("
            nil (str "Unknown error: " (.getMessage message))
            (str "Unknown error type " error))
          message))

(defn- render-error-response [messages]
  (->> messages
       (filter ex-data)
       (map user-error-message)
       (s/join "<br/>")
       ->error-response))

(defn- format-show-message [mention]
  (->> (repo/get-fame (:name mention))
       (r/map #(str (:mention_name mention) " has " % " fame"))))

(defn show [message]
  (->> (msg/command-args message)
       (r/bind #(msg/find-mention message %))
       (r/bind format-show-message)))


(defn leaders [message]
  (->> (repo/leaders 100)
       (r/map format-leaders)))

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

(defn find-handler [message]
  (->> (msg/command message)
       (r/map handlers)
       (r/either #(if (nil? %1)
                    (r/fail (conj %2 (e/invalid-command)))
                    (r/succeed %1 %2))
                 r/fail)))


(defn dispatch
  "Parses and dispatches message to apropriate command"
  [message]
  (->> message
       ((juxt find-handler r/succeed))
       (apply r/apply)
       (r/bind identity)
       (r/map ->response)
       (r/get-or-default render-error-response)))

;; find command handler -> dispatch message to handler
;; on error return invalid command message

(comment

  Trying to recreate the functionality with rail


  (->> m-leaders
       ((juxt find-handler r/succeed))
       (apply r/apply)
       (r/bind identity)
       (r/map ->response)
       (r/get-or-default render-error-response)
       )

  (def m-invalid
    {:event "room_message"
     :item {:message {:from {:id 1 :mention_name "Blinky" :name "Blinky the Fish"}
                      :mentions [{:id 2
                                  :mention_name "sos"
                                  :name "Sweet Sauce"
                                  }]
                      :message "/fame invalid command"}
            :room {:name "test-room"}}})

  (def m-add
    {:event "room_message"
     :item {:message {:from {:id 1 :mention_name "Blinky" :name "Blinky the Fish"}
                      :mentions [{:id 2
                                  :mention_name "sos"
                                  :name "Sweet Sauce"
                                  }]
                      :message "/fame add @sos 10 some reason"}
            :room {:name "test-room"}}})

  (def m-show
    {:event "room_message"
     :item {:message {:from {:mention_name "Blinky" :name "Blinky the Fish"}
                      :mentions [{:mention_name "sos"
                                  :name "Sweet Sauce"
                                  }]
                      :message "/fame show sos"}
            :room {:name "test-room"}}})

  (def m-leaders
    {:event "room_message"
     :item {:message {:from {:mention_name "Blinky" :name "Blinky the Fish"}
                      :mentions [{:mention_name "sos"
                                  :name "Sweet Sauce"
                                  }]
                      :message "/fame leaders"}
            :room {:name "test-room"}}}))
