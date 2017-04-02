(ns fejmetr.message-test
  (:require [clojure.test :refer :all]
            [fejmetr.message :as msg]
            [fejmetr.error :as e]
            [rail.core :as r]
            ))

(def add-message
  {:event "room_message"
   :item {:message {:from {:mention_name "Blinky" :name "Blinky the Fish"}
                    :mentions [{:mention_name "sos"
                                :name "Sweet Sauce"
                                }]
                    :message "/fame add @sos 10 some reason"}
          :room {:name "test-room"}}})

(deftest getting-event-type
  (is (= (msg/event-type add-message)
         "room_message"
         ))
  )

(deftest getting-command-type
  (is (= (msg/command add-message)
         (r/succeed "add")
         )))

(deftest getting-command-args
  (is (= (msg/command-args add-message)
         (r/succeed "@sos 10 some reason")
         ))
  )

(deftest clearing-mentions
  (is (= (msg/clear-mention "@sos")
         "sos"
         )))

(deftest getting-mentions
  (is (= (msg/mentions add-message)
         [{:mention_name "sos"
           :name "Sweet Sauce"
           }]
         )))

(deftest finding-mention
  (testing "finding mention"
           (is (= (msg/find-mention add-message "sos")
                  (r/succeed {:mention_name "sos"
                              :name "Sweet Sauce"})
                  )))
  (testing "error message when mention not found"
           (is (= (r/map-messages ex-data (msg/find-mention add-message "invalid-mention"))
                  (r/map-messages ex-data (r/fail (e/mention-not-found "invalid-mention")))
                  ))))

(deftest sender
  (is (= (msg/sender add-message)
         (r/succeed {:mention_name "Blinky" :name "Blinky the Fish"})
         )))
