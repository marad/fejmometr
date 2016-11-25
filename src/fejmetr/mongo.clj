(ns fejmetr.mongo
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [mount.core :refer [defstate]]))

(def coll "fame")
(def db (atom nil))

(defstate conn
  :start (let [{conn :conn database :db}
               (mg/connect-via-uri (or (System/getenv "MONGODB_URI")
                                       "mongodb://localhost:27017/fejmetr"))]
           (reset! db database)
           conn)
  :stop (do (mg/disconnect conn)
            (reset! db nil)))

(defn get-record [user]
  (let [result (mc/find-maps @db coll {"_id" user})]
    (if (pos? (count result))
      (nth result 0)
      nil)))

(defn get-fame [user]
  (reduce + (map :amount (:donations (get-record user)))))

(defn- ensure-record-exists [user]
  (if-let [record (get-record user)]
    :ok
    (mc/insert @db coll {"_id" user
                        :donations []})))

(defn add-fame [user from reason amount time]
  (ensure-record-exists user)
  (mc/find-and-modify @db coll {"_id" user}
                      {"$push" {:donations {:from from
                                            :reason reason
                                            :amount amount
                                            :timestamp time}}}
                      {"upsert" true}))

(defn leaders [amount]
  (->> (mc/find-maps @db coll)
       (map :_id)
       (map (juxt identity get-fame))
       (sort-by #(get % 1))
       reverse
       (take amount)))
