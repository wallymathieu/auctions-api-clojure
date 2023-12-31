(ns auctions.store-test
  (:require [auctions.migration :refer [migrate]]
            [auctions.samples :refer [sample-auction]]
            [auctions.store :refer [add-bid create-auction
                                    get-auction]]
            [clojure.test :refer [deftest is]]
            [next.jdbc :as jdbc])
  (:import [java.time LocalDateTime]))

(def db-config {:dbtype "h2:mem" :dbname "store-tests"})

(def db (jdbc/get-datasource db-config))
(migrate db-config)

(deftest test-resource
  (let [auction (create-auction db (merge sample-auction {:seller "seller"}))
        auction-id (:id auction)
        existing-auction (get-auction db auction-id)
        not-found (get-auction db 999)]
    (is (= (:title sample-auction) (:title existing-auction)))
    (is (= 1 auction-id))
    (is (= nil not-found))
    (let [auction-with-bid (add-bid db {:bidder "bidder" :amount 100 :at (LocalDateTime/now)} auction-id)]
      (is (= 1 (:id auction-with-bid)))
      (is (= 1 (count (:bids auction-with-bid)))))
    (let [missing-auction (add-bid db {:bidder "bidder" :amount 100 :at (LocalDateTime/now)} 999)]
      (is (= nil missing-auction)))))
