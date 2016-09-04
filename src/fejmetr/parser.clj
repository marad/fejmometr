(ns fejmetr.parser
  (:require [clojure.string :as s]))

(defn parse-command [command]
  (zipmap [:name :args]
          (->> (s/split command #"\s+" 3)
               (drop 1))))

