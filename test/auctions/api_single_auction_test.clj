(ns auctions.api-single-auction-test
  (:require [auctions.api-helpers :refer [request]]
            [auctions.migration :refer [migrate]]
            [auctions.samples :refer [buyer sample-auction seller]]
            [auctions.store :refer [db-from-ds]]
            [clojure.test :refer :all]
            [next.jdbc :as jdbc]))

(def db-config {:dbtype "h2:mem" :dbname "single-auction-tests"})

(def ds (jdbc/get-datasource db-config))
(def db (db-from-ds ds))
(migrate db-config)


(deftest test-resource
  (let [auction-response (request db :post seller "/auctions" sample-auction)
        auction-id (-> auction-response :body :id)
        expected-auction (merge sample-auction {:id auction-id, :url (str "https://localhost/auctions/" auction-id), :seller "a1", :bids []})]
    (is (= 1
           auction-id))
    ;(is (= {:status 200 :body expected-auction}
    ;       auction-response))

    (is (= {:status 200 :body expected-auction}
           (request db :get seller (str "/auctions/" auction-id))))
    (is (= {:status 404 :body nil}
           (request db :get seller (str "/auctions/" 99))))
    (is (= {:status 403 :body {:cause "not-authorized"}}
           (request db :post nil "/auctions" sample-auction)))
    (is (= {:status 200 :body [expected-auction]}
           (request db :get buyer "/auctions")))))
