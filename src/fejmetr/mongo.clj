(ns fejmetr.mongo
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [mount.core :refer [defstate]]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [rail.core :as r]
            [fejmetr.error :as e]
            ))

(def coll "fame")
(def db (atom nil))

(defn- get-db []
  (r/fail-if-nil (e/not-connected-to-db) @db))

(defstate conn
  :start (let [{conn :conn database :db}
               (mg/connect-via-uri (or (System/getenv "MONGODB_URI")
                                       "mongodb://localhost:27017/fejmetr"))]
           (reset! db database)
           conn)
  :stop (do (mg/disconnect conn)
            (reset! db nil)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Private parts

(defn- calc-fame [user-record]
  (let [from-time (t/minus (t/now) (-> 2 t/weeks))
        timestamp (tc/to-long from-time)]
    (->> user-record
         :donations
         (filter #(< timestamp (:timestamp %)))
         (map :amount)
         (reduce +))))

(defn- calc-leaders [amount records]
  (->> records
       (map (juxt :_id calc-fame))
       (sort-by #(get % 1))
       reverse
       (take amount)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public

(defn get-record [user]
  (->> (get-db)
       (r/map #(mc/find-maps % coll {"_id" user}))
       (r/map first)
       (r/either (r/fail-if-nil (e/user-not-found user))
                 r/fail)))

(defn get-fame [user]
    (->> (get-record user)
         (r/map calc-fame)))

(defn add-fame [user from reason amount time]
  (let [donation {:from from
                  :reason reason
                  :amount amount
                  :timestamp time}]
    (->> (get-record user)
         (r/either (fn [_ _]
                       (->> (get-db)
                            (r/map #(mc/find-and-modify % coll {"_id" user}
                                                        {"$push" {:donations donation}}
                                                        {"upsert" true}))))
                   (fn [_]
                       (->> (get-db)
                            (r/map #(mc/insert % coll
                                               {"_id" user :donations [donation]}))))))))

(defn leaders [amount]
  (->> (get-db)
       (r/map #(mc/find-maps % coll))
       (r/map (partial calc-leaders amount))))
