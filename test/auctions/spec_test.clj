(ns auctions.spec-test
  (:require [auctions.spec :refer [AuctionId AuctionResult]]
            [clojure.test :refer [deftest is testing]]
            [malli.core :as m])
  ;(:import (java.time LocalDateTime ZonedDateTime))
 )

(let [valid-user "BuyerOrSeller|1|test@test.se"
      valid-auction {:title "auction"
                     :startsAt "2023-03-15T11:50:55Z"
                     :expiry "2023-03-16T11:50:55Z"
                     :seller valid-user
                     :currency "SEK"}
      valid-auction-with-url-and-bids (merge valid-auction {:id 1,
                                                            :url "https://localhost/auctions/1",
                                                            :bids []})
      ; valid-auction-with-local-time (merge valid-auction {:startsAt (LocalDateTime/parse "2023-03-15T11:50:55") :expiry (LocalDateTime/parse "2023-03-16T11:50:55")})
      ; valid-auction-with-zoned-time (merge valid-auction {:startsAt (ZonedDateTime/parse "2023-03-15T11:50:55Z") :expiry (ZonedDateTime/parse "2023-03-16T11:50:55Z")})
      invalid-auction-without-seller (dissoc  valid-auction :seller)
      ; invalid-auction-with-wrong-time (merge valid-auction {:startsAt "2023-03-15T1:50:55" :expiry "2023-03-1411:50:55"})
      invalid-auction-without-currency (dissoc  valid-auction :currency)

      invalid-auction {}]

  (deftest schema-spec
    (testing "auction id schema is a schema"
      (is (true? (m/schema? AuctionId))))

    (testing "auction schema is a schema"
      (is (true? (m/schema? AuctionResult))))
    (testing "invalid auction"
      (is (false? (m/validate AuctionResult invalid-auction)))
      (is (false? (m/validate AuctionResult invalid-auction-without-seller)))
      ; (is (false? (m/validate auction-schema invalid-auction-with-wrong-time)))
      (is (false? (m/validate AuctionResult invalid-auction-without-currency))))
    (testing "a valid auction"
      (is (true? (m/validate AuctionResult valid-auction)))
      (is (= valid-auction (m/coerce AuctionResult valid-auction)))
      ; (is (true? (m/validate auction-schema valid-auction-with-local-time)))
      ; (is (true? (m/validate auction-schema valid-auction-with-zoned-time)))
      (is (true? (m/validate AuctionResult valid-auction-with-url-and-bids))))))
