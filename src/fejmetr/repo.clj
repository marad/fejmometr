(ns fejmetr.repo)

(defonce db (atom {}))

; W faktycznej implementacji powinno sie zbierac PER pokój

(defn get-record [user]
  (get @db user))

(defn get-fame [user]
  (reduce + (map :amount (get-record user))))

(defn add-fame [user from reason amount]
  (swap! db #(update % user conj
                     {:from from
                      :reason reason
                      :amount amount
                      :timestamp 1234
                      }
                     ))
  )

(defn leaders [amount]
  (->> @db
    keys
    (map (juxt identity get-fame))
    (sort-by #(get % 1))
    reverse
    (take amount)
    )
  )
