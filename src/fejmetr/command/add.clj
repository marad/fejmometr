(ns fejmetr.command.add
  (:require [clojure.string :as str]
            [fejmetr.repo :as repo]
            [fejmetr.message :as msg]
            [fejmetr.error :as e]
            [schema.core :as s]
            [rail.core :as r]
            [rail.combinators :as rc]
            ))

(def AddArgs {:receivers [s/Str]
              :amount s/Num
              :reason s/Str})

(def HCResponse {:color s/Str
                 :message s/Str})

(def TokenType (s/enum :mention :number :text))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Argument Parsing

(s/defn token-type :- TokenType
  [token :- s/Str]
  (cond
    (str/starts-with? token "@") :mention
    (re-matches #"(-?[0-9]+\/[0-9]+)|(-?[0-9]+(.[0-9]+)?)" token) :number
    :else :text))

(s/defn remove-first-number :- [s/Str]
  [tokens :- [s/Str]]
  (let [[m n] (split-with #(not= (token-type %) :number) tokens)]
    (concat m (rest n))))


(s/defn create-args :- r/Result ;; of AddArgs
  [arg-line :- s/Str]
  (let [mention? #(= (token-type %) :mention)
        num? #(= (token-type %) :number)
        get-receivers #(->> % (filter mention?)
                            (map msg/clear-mention)
                            r/succeed)
        get-amount #(->> % (filter num?) first
                         (r/fail-if-nil (e/amount-not-provided))
                         (r/map read-string))
        get-reason #(->> %
                         remove-first-number
                         (remove mention?)
                         (str/join " ")
                         r/succeed)
        tokens (str/split arg-line #"\s+")]
    (->> tokens
         ((juxt get-receivers get-amount get-reason))
         rc/vector
         (r/map #(zipmap [:receivers :amount :reason] %)))))

(s/defn parse-args :- r/Result ;; of AddArgs
  [message :- msg/Message]
  (->> (msg/command-args message)
       (r/bind create-args)))


(s/defn prepare-args :- r/Result ;; of [reason amount sender receivers]
  [message :- msg/Message]
  (let [args (parse-args message)]
    (rc/vector [(r/map :reason args)
                (r/map :amount args)
                (msg/sender message)
                (r/bind #(->> (:receivers %)
                              (map (partial msg/find-mention message))
                              (rc/vector)
                              (r/map-messages (fn [msg]
                                                  (if (= msg :mention-not-found)
                                                    :missing-receiver
                                                    msg)))
                              ) args)])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Message validators

(s/defn cannot-add-to-self-validator ;; :- [s/Str s/Num msg/Mention [msg/Mention]]
  [[_ _ sender receivers :as args]]
  (if ((set (map :id receivers)) (:id sender))
    (r/fail (e/cannot-add-fame-to-self))
    (r/succeed args)))

(s/defn receivers-not-empty-validator ;; :- [s/Str s/Num msg/Mention [msg/Mention]]
  [[_ _ _ receivers :as args]]
  (if (empty? receivers)
    (r/fail (e/receivers-cannot-be-empty))
    (r/succeed args)))

(s/defn amount-validator [[_ amount _ _ :as args]] ;; :- [s/Str s/Num msg/Mention [msg/Mention]]
  (if-not (pos? amount)
          (r/fail (e/amount-not-positive))
          (r/succeed args)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn dispach-fame [[reason amount sender receivers :as args]]
    (->> receivers
         (map #(repo/add-fame (:name %)
                              (:name sender)
                              reason
                              amount
                              (. System currentTimeMillis)))
         rc/vector
         (r/either (fn [_ msgs] (r/succeed args msgs))
                   r/fail)))

(s/defn show-receivers :- s/Str
  [receivers]
  (let [receivers (map :mention_name receivers)]
    (loop [acc (first receivers)
           tail (rest receivers)]
          (case (count tail)
            0 acc
            1 (str acc " and " (first tail))
            (recur (str acc ", " (first tail)) (rest tail))))))

(s/defn render-summary :- s/Str
  [[reason amount sender receivers]]
  (str (show-receivers receivers) " got " amount " fame from "
                         (:mention_name sender) " for: " reason))

(s/defn execute :- r/Result ;; of s/Str
  [message :- msg/Message]
  (->> (prepare-args message)
       (r/bind cannot-add-to-self-validator)
       (r/bind receivers-not-empty-validator)
       (r/bind amount-validator)
       (r/bind dispach-fame)
       (r/map render-summary)))

(comment

  (def message
    {:event "room_message"
     :item {:message {:from {:mention_name "Blinky" :name "Blinky the Fish"}
                      :mentions [{:mention_name "sos"
                                  :name "Sweet Sauce"
                                  }]
                      :message "/fame add @sos 10 some reason"}
            :room {:name "test-room"}}})

  (def message
    {:event "room_message"
     :item {:message {:from {:id 1 :mention_name "Blinky" :name "Blinky the Fish"}
                      :mentions [{:id 2
                                  :mention_name "sos"
                                  :name "Sweet Sauce"
                                  }
                                 {:id 1
                                  :mention_name "Blinky"
                                  :name "Blinky The Fish"}]
                      :message "/fame add @sos 10 some reason"}
            :room {:name "test-room"}}})
  )
