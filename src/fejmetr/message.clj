(ns fejmetr.message
  (:require [clojure.string :as str]
            [schema.core :as s]))

(def Mention {:id s/Str :mention_name s/Str :name s/Str})
(def Message
  {:event s/Str
   :item {:message {:from Mention
                    :mentions [Mention]
                    :message s/Str}
          :room {:name s/Str}}})

(s/defn event-type :- s/Str
  [message :- Message]
  (:event message))

(s/defn command :- s/Str
  [message :- Message]
  (-> (get-in message [:item :message :message])
       (str/split #"\s+" 3)
       (nth 1)
       ))

(s/defn command-args :- s/Str
  [message :- Message]
  (-> (get-in message [:item :message :message])
      (str/split #"\s+" 3)
      (nth 2)))

(s/defn clear-mention :- s/Str
  [mention :- s/Str]
  (.replaceAll (str/trim mention) "@" ""))

(s/defn mentions :- [Mention]
  [message :- Message]
  (get-in message [:item :message :mentions]))

(s/defn find-mention :- (s/maybe Mention)
  [message :- Message
   mentioned :- s/Str]
  (->> (get-in message [:item :message :mentions])
       (filter #(= (clear-mention mentioned) (:mention_name %)))
       first))

(s/defn sender :- Mention
  [message :- Message]
  (get-in message [:item :message :from]))
