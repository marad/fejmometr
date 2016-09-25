(ns fejmetr.repo
  (:require [fejmetr.mongo :as mongo]))


; W faktycznej implementacji powinno sie zbierac PER pokój
; implementacja in-memory
;(defonce db (atom {}))
;(defn get-record [user]
;  (get @db user))
;
;(defn get-fame [user]
;  (reduce + (map :amount (get-record user))))
;
;(defn add-fame [user from reason amount]
;  (swap! db #(update % user conj
;                     {:from from
;                      :reason reason
;                      :amount amount
;                      :timestamp 1234
;                      }
;                     ))
;  )
;
;(defn leaders [amount]
;  (->> @db
;    keys
;    (map (juxt identity get-fame))
;    (sort-by #(get % 1))
;    reverse
;    (take amount)))


(def get-record mongo/get-record)
(def get-fame mongo/get-fame)
(def add-fame mongo/add-fame)
(def leaders mongo/leaders)
