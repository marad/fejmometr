(ns fejmetr.fixtures
    (:require [fejmetr.web :as web]
              [mount.core :as mount]
              ))

(defn app [t]
  (mount/start)
  (t)
  (mount/stop))

