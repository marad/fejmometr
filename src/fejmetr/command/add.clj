(ns fejmetr.command.add
  (:require [clojure.string :as s]
            [fejmetr.repo :as repo]
            ))

(defn clear-mention [mention]
  (.replaceAll mention "@" ""))

(defn find-mentioned [message mentioned]
  (->> (get-in message [:item :message :mentions])
       (filter #(= mentioned (:mention_name %)))
       first))

(defn execute [message args]
  (let [giver-mention (get-in message [:item :message :from :mention_name])
        giver (get-in message [:item :message :from :name])
        [taker amount reason] (s/split args #"\s" 3)
        taker (clear-mention taker)
        ]
    (println ">> " (find-mentioned message taker))
    (cond
      (= taker giver-mention) {:color "red"
                               :message "That's cheating!"
                               }
      (nil? (find-mentioned message taker)) {:color "red"
                                             :message (str "Who is " taker "?")
                                             }
      :else (do (repo/add-fame taker giver reason (read-string amount))
                {:color "green"
                 :message (str taker " got " amount " fame from "
                               giver-mention "; reason: " reason)})
      )))
