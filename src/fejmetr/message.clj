(ns fejmetr.message
  (:require [clojure.string :as s]))

(defn command [message]
  (-> (get-in message [:item :message :message])
       (s/split #"\s" 3)
       (nth 1)
       ))

(defn command-args [message]
  (-> (get-in message [:item :message :message])
      (s/split #"\s" 3)
      (nth 2)))

(defn clear-mention [mention]
  (.replaceAll (s/trim mention) "@" ""))

(defn mentions [message]
  (get-in message [:item :message :mentions]))

(defn find-mention [message mentioned]
  (->> (get-in message [:item :message :mentions])
       (filter #(= (clear-mention mentioned) (:mention_name %)))
       first))

(defn sender [message]
  (get-in message [:item :message :from]))
