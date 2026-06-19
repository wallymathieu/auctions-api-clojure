(ns auctions.api-single-auction-with-bid-test
  (:require [auctions.api-helpers :refer [request]]
            [auctions.migration :refer [migrate]]
            [auctions.samples :refer [buyer sample-auction seller]]
            [clojure.test :refer [deftest is]]
            [next.jdbc :as jdbc]))

(def db-config {:dbtype "h2:mem" :dbname "single-auction-with-bids-tests"})

(def db (jdbc/get-datasource db-config))
(migrate db-config)


(deftest test-resource
  (let [auction-id (-> (request db :post seller "/auctions" sample-auction) :body :auction :id)
        bid-response (request db :post buyer (str "/auctions/" auction-id "/bids") {:amount 10})
        expected-auction (merge sample-auction {:id auction-id,
                                                :url (str "https://localhost/auctions/" auction-id),
                                                :seller "a1",
                                                :bids [{:bidder "a2", :amount 10}],
                                                :winner nil,
                                                :winnerPrice nil})]
    (is (= 1 auction-id))
    (is (= 200 (:status bid-response)))
    (is (= "BidAccepted" (-> bid-response :body :$type)))
    (is (= {:auction auction-id :amount 10 :bidder "a2"}
           (-> bid-response :body :bid)))
    (is (= {:status 200 :body expected-auction}
           (request db :get buyer (str "/auctions/" auction-id))))
    (is (= {:status 400 :body  {:value {},
                                :in ["request" "body-params"],
                                :humanized {:amount ["missing required key"]}}}
           (request db :post buyer (str "/auctions/" auction-id "/bids") {})))
    (is (= {:status 400 :body  {:value {:amount "x"},
                                :in ["request" "body-params"],
                                :humanized {:amount ["should be an integer"]}}}
           (request db :post buyer (str "/auctions/" auction-id "/bids") {:amount "x"})))
    (is (= {:status 404 :body {:type "AuctionNotFound" :auctionId 99}}
           (request db :post buyer (str "/auctions/" 99 "/bids") {:amount 10})))
    (is (= {:status 400 :body {:type "MustPlaceBidOverHighestBid",
                               :amount 10,
                               :auctionId auction-id}}
           (request db :post buyer (str "/auctions/" auction-id "/bids") {:amount 5})))
    (is (= {:status 400 :body {:type "SellerCannotPlaceBids" :auctionId auction-id}}
           (request db :post seller (str "/auctions/" auction-id "/bids") {:amount 20})))))
