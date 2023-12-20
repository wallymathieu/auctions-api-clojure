(ns auctions.store-test
  (:require [auctions.migration :refer [migrate]]
            [auctions.samples :refer [sample-auction]]
            [auctions.store :refer [add-bid create-auction db-from-ds
                                    get-auction]]
            [clojure.test :refer :all]
            [next.jdbc :as jdbc]))

(def db-config {:dbtype "h2:mem" :dbname "store-tests"})

(def ds (jdbc/get-datasource db-config))
(def db (db-from-ds ds))
(migrate db-config)
(deftest test-resource
  (let [auction (create-auction db (merge sample-auction {:seller "seller"}))
        auction-id (:id auction)
        existing-auction (get-auction db auction-id)
        not-found (get-auction db 999)]
    (is (= (:title sample-auction) (:title existing-auction)))
    (is (= 1 auction-id))
    (is (= nil not-found))
    (let [auction-with-bid (add-bid db {:bidder "bidder" :amount 100 :at (java.time.LocalDateTime/now)} auction-id)]
      (is (= 1 (:id auction-with-bid)))
      (is (= 1 (count (:bids auction-with-bid)))))))
