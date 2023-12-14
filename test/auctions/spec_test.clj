(ns auctions.spec-test
  (:require [auctions.spec :refer :all]
            [clojure.test :refer :all]
            [malli.core :as m]))

(let [valid-user "BuyerOrSeller|1|test@test.se"
      invalid-user "test"
      valid-auction {:title "auction"
                     :startsAt "2023-03-15T11:50:55"
                     :expiry "2023-03-16T11:50:55"
                     :user valid-user
                     :currencyCode "SEK"}
      invalid-auction2 (merge valid-auction {:user invalid-user})
      invalid-auction {}]

  (deftest schema-spec
    (testing "invalid user"
      (is (= false (m/validate user-schema invalid-user))))
    (testing "valid user"
      (is (= true (m/validate user-schema valid-user))))

    (testing "invalid auction"
      (is (= false (m/validate auction-schema invalid-auction)))
      (is (= false (m/validate auction-schema invalid-auction2))))
    (testing "a valid auction"
      (is (= true (m/validate auction-schema valid-auction))))))
