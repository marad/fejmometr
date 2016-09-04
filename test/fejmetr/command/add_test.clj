(ns fejmetr.command.add-test
  (:require [clojure.test :refer :all]
            [fejmetr.command.add :as add]
            [fejmetr.repo :as repo]
            ))

(deftest adding-fame
  (let [add-message {:item {:message {:from {:id "1" :mention_name "Blinky" :name "Blinky the Fish"}
                                      :mentions [{:id "2"
                                                  :mention_name "sos"
                                                  :name "Sweet Sauce"
                                                  }]
                                      :message "/fame add @sos 10 some reason"}
                            :room {:name "test-room"}}}]
    (is (= (add/execute add-message)
           {:color "green"
            :message "sos got 10 fame from Blinky; reason: some reason"
            }
           ))))

(deftest cannot-add-self
  (let [add-self-message {:item {:message {:from {:id "1" :mention_name "Blinky" :name "Blinky the Fish"}
                                           :mentions [{:id "1"
                                                       :mention_name "Blinky"
                                                       :name "Blinky the Fish"
                                                       }]
                                           :message "/fame add @Blinky 10 some reason"}
                                 :room {:name "test-room"}}}]
    (is (= (add/execute add-self-message)
           {:color "red"
            :message "That's cheating!"
            }
           )))
  )

(deftest cannot-add-without-mention
  (let [add-message {:item {:message {:from {:id "1" :mention_name "Blinky" :name "Blinky the Fish"}
                                      :mentions []
                                      :message "/fame add @sos 10 some reason"}
                            :room {:name "test-room"}}}]
    (is (= (add/execute add-message)
           {:color "red"
            :message "Who is sos?"
            }
           ))))

