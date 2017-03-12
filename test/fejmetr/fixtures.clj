(ns fejmetr.fixtures
    (:require [fejmetr.web :as web]
              [fejmetr.mongo :refer [coll db]]
              [mount.core :as mount]
              [monger.db :as md]
              [monger.collection :as mc]
              ))

(defn app [t]
  (mount/start)
  (t)
  (mount/stop))

(defn clear-db [t]
  (mc/drop @db coll)
  (t))

