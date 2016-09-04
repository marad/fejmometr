(ns fejmetr.command.add-test
  (:require [clojure.test :refer :all]
            [fejmetr.command.add :as add]
            [fejmetr.repo :as repo]
            ))

(deftest adding-fame
  (let [add-message {:item {:message {:from {:mention_name "Blinky" :name "Blinky the Fish"}
                                      :mentions [{:mention_name "sos"
                                                  :name "Sweet Sauce"
                                                  }]
                                      :message "/fame add @sos 10 some reason"}
                            :room {:name "test-room"}}}]
    (is (= (add/execute add-message "@sos 10 some reason")
           {:color "green"
            :message "sos got 10 fame from Blinky; reason: some reason"
            }
           ))))

(deftest cannot-add-self
  (let [add-self-message {:item {:message {:from {:mention_name "Blinky" :name "Blinky the Fish"}
                                           :mentions []
                                           :message "/fame add @Blinky 10 some reason"}
                                 :room {:name "test-room"}}}]
    (is (= (add/execute add-self-message "@Blinky 10 reason")
           {:color "red"
            :message "That's cheating!"
            }
           )))
  )

(deftest cannot-add-without-mention
  (let [add-message {:item {:message {:from {:mention_name "Blinky" :name "Blinky the Fish"}
                                      :mentions []
                                      :message "/fame add @sos 10 some reason"}
                            :room {:name "test-room"}}}]
    (is (= (add/execute add-message "@sos 10 some reason")
           {:color "red"
            :message "Who is sos?"
            }
           ))))

