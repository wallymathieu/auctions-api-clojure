(ns auctions.api-single-auction-test
  (:require [auctions.api-helpers :refer [request]]
            [auctions.migration :refer [migrate]]
            [auctions.samples :refer [buyer sample-auction seller]]
            [clojure.test :refer [deftest is]]
            [next.jdbc :as jdbc]))

(def db-config {:dbtype "h2:mem" :dbname "single-auction-tests"})

(def db (jdbc/get-datasource db-config))
(migrate db-config)


(deftest test-resource
  (let [auction-response (request db :post seller "/auctions" sample-auction)
        auction-id (-> auction-response :body :id)
        auction-without-currency-response (request db :post seller "/auctions" (dissoc sample-auction :currency))
        expected-auction (merge sample-auction {:id auction-id, :url (str "https://localhost/auctions/" auction-id), :seller "a1", :bids []})]
    (is (= 1
           auction-id))
    (is (= 400 (:status auction-without-currency-response)))
    (is (= {:currency ["missing required key"]}
       (-> auction-without-currency-response :body :humanized)))

    (is (= {:status 200 :body expected-auction}
           (request db :get seller (str "/auctions/" auction-id))))
    (is (= {:status 404 :body nil}
           (request db :get seller (str "/auctions/" 99))))
    (is (= {:status 403 :body {:cause "not-authorized"}}
           (request db :post nil "/auctions" sample-auction)))
    (is (= {:status 200 :body [expected-auction]}
           (request db :get buyer "/auctions")))))
