(ns auctions.spec-test
  (:require [auctions.spec :refer :all]
            [clojure.test :refer :all]
            [malli.core :as m]))

(let [valid-user "BuyerOrSeller|1|test@test.se"
      valid-auction {:title "auction"
                     :startsAt "2023-03-15T11:50:55Z"
                     :expiry "2023-03-16T11:50:55Z"
                     :seller valid-user
                     :currency "SEK"}
      valid-auction-with-url-and-bids (merge valid-auction {:id 1,
                                            :url "https://localhost/auctions/1",
                                            :bids []})
      invalid-auction-without-seller (dissoc  valid-auction :seller)
      invalid-auction-with-local-time (merge valid-auction {:startsAt "2023-03-15T11:50:55" :expiry "2023-03-14T11:50:55"})
      invalid-auction-without-currency (dissoc  valid-auction :currency)

      invalid-auction {}]

  (deftest schema-spec
    (testing "auction id schema is a schema"
      (is (true? (m/schema? auction-id-schema))))

    (testing "auction schema is a schema"
      (is (true? (m/schema? auction-schema))))
    (testing "invalid auction"
      (is (false? (m/validate auction-schema invalid-auction)))
      (is (false? (m/validate auction-schema invalid-auction-without-seller)))
      (is (false? (m/validate auction-schema invalid-auction-with-local-time)))
      (is (false? (m/validate auction-schema invalid-auction-without-currency))))
    (testing "a valid auction"
      (is (true? (m/validate auction-schema valid-auction)))
      (is (true? (m/validate auction-schema valid-auction-with-url-and-bids))))))
