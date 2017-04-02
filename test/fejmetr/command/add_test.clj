(ns fejmetr.command.add-test
  (:require [clojure.test :refer :all]
            [schema.test :refer [validate-schemas]]
            [fejmetr.command.add :as add]
            [fejmetr.repo :as repo]
            [fejmetr.fixtures :refer [app clear-db]]
            [fejmetr.error :as e]
            [rail.core :as r]
            ))


(use-fixtures :once (join-fixtures [validate-schemas app]))
(use-fixtures :each clear-db)


(deftest adding-fame
  (let [add-message {:event "room_message"
                     :item {:message {:from {:id "1" :mention_name "Blinky" :name "Blinky the Fish"}
                                      :mentions [{:id "2"
                                                  :mention_name "sos"
                                                  :name "Sweet Sauce"}]
                                      :message "/fame add @sos 10 some reason"}
                            :room {:name "test-room"}}}]
    (is (= (add/execute add-message)
           (r/succeed "sos got 10 fame from Blinky for: some reason")
           ))
    (is (= (repo/get-fame "Sweet Sauce") (r/succeed 10)))))


(deftest adding-fame-to-multiple-people
  (let [add-message {:event "room_message"
                     :item {:message {:from {:id "1" :mention_name "Blinky" :name "Blinky the Fish"}
                                      :mentions [{:id "2"
                                                  :mention_name "sos"
                                                  :name "Sweet Sauce"}
                                                 {:id "3"
                                                  :mention_name "peper"
                                                  :name "Hot Peper"}
                                                 {:id "4"
                                                  :mention_name "salt"
                                                  :name "Sour Salt"}]
                                      :message "/fame add @sos @peper 10 @salt some reason"}
                            :room {:name "test-room"}}}]
    (is (= (add/execute add-message)
           (r/succeed  "sos, peper and salt got 10 fame from Blinky for: some reason")))
    (is (= (repo/get-fame "Sweet Sauce") (r/succeed 10)))
    (is (= (repo/get-fame "Hot Peper") (r/succeed 10)))
    (is (= (repo/get-fame "Sour Salt") (r/succeed 10)))
    ))


(deftest cannot-add-self
  (let [add-self-message {:event "room_message"
                          :item {:message {:from {:id "1" :mention_name "Blinky" :name "Blinky the Fish"}
                                           :mentions [{:id "1"
                                                       :mention_name "Blinky"
                                                       :name "Blinky the Fish"
                                                       }]
                                           :message "/fame add @Blinky 10 some reason"}
                                 :room {:name "test-room"}}}]
    (is (= (r/map-messages ex-data (add/execute add-self-message))
           (r/map-messages ex-data (r/fail (e/cannot-add-fame-to-self)))
           ))))


(deftest cannot-add-without-mention
  (let [add-message {:event "room_message"
                     :item {:message {:from {:id "1" :mention_name "Blinky" :name "Blinky the Fish"}
                                      :mentions []
                                      :message "/fame add @sos 10 some reason"}
                            :room {:name "test-room"}}}]
    (is (= (r/map-messages ex-data (add/execute add-message))
           (r/map-messages ex-data (r/fail (e/mention-not-found "sos")))))))

