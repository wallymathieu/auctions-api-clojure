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
      invalid-auction2 (merge valid-auction {:auctions/user invalid-user})
      ]

  (deftest schema-spec
    (testing "invalid user"
      (is (= false (s/valid? :auctions/user invalid-user))))
    (testing "valid user"
      (is (= true (s/valid? :auctions/user valid-user))))
    
    (testing "invalid auction"
      (is (= false (s/valid? :auctions/auction invalid-auction)))
      (is (= false (s/valid? :auctions/auction invalid-auction2))))
    (testing "a valid auction"
      (is (= true (s/valid? :auctions/auction valid-auction)))
      (is (= valid-auction (s/conform :auctions/auction valid-auction)))
      )))