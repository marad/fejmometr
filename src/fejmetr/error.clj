(ns fejmetr.error)

(defn invalid-command []
  (ex-info "Invalid command" {:type :invalid-command}))

;; DB Errors
(defn not-connected-to-db []
  (ex-info "Not connected to db" {:type :not-connected-to-db}))

(defn user-not-found [user]
  (ex-info "User not found" {:type :user-not-fund
                             :user user}))

;; Message Errors
(defn command-not-specified []
  (ex-info "Command not specified" {:type :command-not-specified}))

(defn arguments-not-specified []
  (ex-info "Argumens not specified" {:type :arguments-not-specified}))

(defn mention-not-found [mention]
  (ex-info "Mention not found" {:type :mention-not-found
                                :mention mention}))

(defn missing-sender []
  (ex-info "Missing sender" {:type :missing-sender}))


;; Add Errors
(defn amount-not-provided []
  (ex-info "Amount not provided" {:type :amount-not-provided}))

(defn cannot-add-fame-to-self []
  (ex-info "Cannot add fame to self" {:type :cannot-add-fame-to-self}))

(defn receivers-cannot-be-empty []
  (ex-info "Receivers cannot be empty" {:type :receivers-cannot-be-empty}))

(defn amount-not-positive []
  (ex-info "Amount not positive" {:type :amount-not-positive}))
