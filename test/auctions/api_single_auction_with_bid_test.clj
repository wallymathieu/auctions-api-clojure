(ns auctions.api-single-auction-with-bid-test
  (:require [auctions.api-helpers :refer [request]]
            [auctions.migration :refer [migrate]]
            [auctions.samples :refer [buyer sample-auction seller]]
            [auctions.store :refer [db-from-ds]]
            [clojure.test :refer :all]
            [next.jdbc :as jdbc]))

(def db-config {:dbtype "h2:mem" :dbname "single-auction-with-bids-tests"})

(def ds (jdbc/get-datasource db-config))
(def db (db-from-ds ds))
(migrate db-config)


(deftest test-resource
  (let [auction-id (-> (request db :post seller "/auctions" sample-auction) :body :id)
        expected-auction (merge sample-auction {:id auction-id,
                                                :url (str "https://localhost/auctions/" auction-id),
                                                :seller "a1",
                                                :bids [{:bidder "a2", :amount 10}]})]
    (is (= 1
           auction-id))
    (is (= {:status 200 :body expected-auction}
           (request db :post buyer (str "/auctions/" auction-id "/bids") {:amount 10})))
    (is (= {:status 400 :body  {:value {},
                                :in ["request" "body-params"],
                                :humanized {:amount ["missing required key"]}}}
           (request db :post buyer (str "/auctions/" auction-id "/bids") {})))
    (is (= {:status 404 :body nil}
           (request db :post buyer (str "/auctions/" 99 "/bids") {:amount 10})))))