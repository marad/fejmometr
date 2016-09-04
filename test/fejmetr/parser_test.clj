(ns fejmetr.parser-test
  (:require [fejmetr.parser :as sut]
            [clojure.test :refer :all]))

(testing "should parse command"
  (let [cmd "/fame add @sos 10 for free"]
    (is (= (sut/parse-command cmd)
           {:name "add"
            :args "@sos 10 for free"}))))


