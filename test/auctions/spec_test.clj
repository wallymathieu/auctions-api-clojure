(ns auctions.spec-test
  (:require [auctions.spec :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.test :refer :all]))
(let [invalid-user "test"
      valid-user "BuyerOrSeller|1|test@test.se"
      valid-auction {
                     :auctions/title "auction"
                     :auctions/starts-at (java.time.LocalDateTime/parse "2023-03-15T11:50:55")
                     :auctions/expiry (java.time.LocalDateTime/parse "2023-03-16T11:50:55")
                     :auctions/user valid-user
                     :auctions/currency-code "SEK"}
      invalid-auction {}
      invalid-auction2 (conj valid-auction {:auctions/user invalid-user})
      ]

  (deftest schema-spec
    ;(testing "a valid email."
    ;  (is (= true (s/valid? :acct/email valid-email))))
    (testing "invalid auction"
      (is (= false (s/valid? :auctions/auction invalid-auction)))
      (is (= false (s/valid? :auctions/auction invalid-auction2))))
    (testing "a valid auction"
      (is (= true (s/valid? :auctions/auction valid-auction)))
      (is (= valid-auction (s/conform :auctions/auction valid-auction)))
      )))