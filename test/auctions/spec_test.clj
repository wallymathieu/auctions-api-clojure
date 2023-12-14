(ns auctions.spec-test
  (:require [auctions.spec :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.test :refer :all]))
(def valid-user "BuyerOrSeller|1|test@test.se")
(def valid-auction-1 {"title" "auction"
                       "startsAt" (java.time.LocalDateTime/parse "2023-03-15T11:50:55")
                       "expiry" (java.time.LocalDateTime/parse "2023-03-16T11:50:55")
                       "user" valid-user
                       "currencyCode" "SEK"})
(comment 
  [:map ["title" :string] ["startsAt" :some] ["expiry" :some] ["user" :string] ["currency-code" :string]]
  )
(let [invalid-user "test"
      valid-user "BuyerOrSeller|1|test@test.se"
      valid-auction {"title" "auction"
                     "startsAt" (java.time.LocalDateTime/parse "2023-03-15T11:50:55")
                     "expiry" (java.time.LocalDateTime/parse "2023-03-16T11:50:55")
                     "user" valid-user
                     "currencyCode" "SEK"}
      invalid-auction {}
      invalid-auction2 (merge valid-auction {"user" invalid-user})
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