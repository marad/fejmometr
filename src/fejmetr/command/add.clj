(ns fejmetr.command.add
  (:require [clojure.string :as str]
            [fejmetr.repo :as repo]
            [fejmetr.message :as msg]
            [schema.core :as s]))

(def AddArgs {:receivers [s/Str]
              :amount s/Num
              :reason s/Str})
(def HCResponse {:color s/Str
                 :message s/Str})

(def TokenType (s/enum :mention :number :text))


(s/defn token-type :- TokenType
  [token :- s/Str]
  (cond
    (str/starts-with? token "@") :mention
    (re-matches #"([0-9]+\/[0-9]+)|([0-9]+(.[0-9]+)?)" token) :number
    :else :text))


(s/defn remove-first-number :- [s/Str]
  [tokens :- [s/Str]]
  (let [[m n] (split-with #(not= (token-type %) :number) tokens)]
    (concat m (rest n))))


(s/defn get-reason :- s/Str
  [tokens :- [s/Str]]
  (->> tokens
       remove-first-number
       (remove #(= (token-type %) :mention))
       (str/join " ")))


(s/defn parse-args :- AddArgs
  [message :- msg/Message]
  (let [args (msg/command-args message)
        tokens (str/split args #"\s+")]
    {:receivers (->> tokens (filter #(= (token-type %) :mention)) (map msg/clear-mention))
     :amount (->> tokens (filter #(= (token-type %) :number)) first read-string)
     :reason (get-reason tokens)}))


(s/defn show-receivers :- s/Str
  [receivers :- [msg/Mention]]
  (let [receivers (map :mention_name receivers)]
    (loop [acc (first receivers)
           tail (rest receivers)]
          (case (count tail)
            0 acc
            1 (str acc " and " (first tail))
            (recur (str acc ", " (first tail)) (rest tail))))))


(s/defn execute :- HCResponse
  [message :- msg/Message]
  (let [args (parse-args message)
        sender (msg/sender message)
        receivers (->> (:receivers args) (map (partial msg/find-mention message)) (remove nil?))
        ]
    (cond
      ((set (map :id receivers)) (:id sender))
      {:color "red"
       :message "You cannot add fame to yourself!"}

      (empty? receivers)
      {:color "red"
       :message "Who is the receiver?"}

      ((comp not pos?) (:amount args))
      {:color "red"
       :message (str "That's not adding fame...")}

      :else
      (do
        (doseq [receiver receivers]
               (repo/add-fame (:name receiver)
                              (:name sender)
                              (:reason args)
                              (:amount args)
                              (. System currentTimeMillis)))
          {:color "green"
           :message (str (show-receivers receivers) " got " (:amount args) " fame from "
                         (:mention_name sender) " for: " (:reason args))})
      )))
