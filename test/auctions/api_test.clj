(ns auctions.api-test
  (:require [auctions.core :refer [app-routes]]
            [auctions.migration :refer [migrate]]
            [auctions.store :refer [db-from-ds]]
            [clojure.data.json :as json]
            [clojure.test :refer :all]
            [next.jdbc :as jdbc])
  (:import java.nio.charset.StandardCharsets))
;; https://dev.solita.fi/2021/03/05/automatically-generated-api-tests-with-clojure-and-reitit.html
(def db-config {:dbtype "h2:mem" :dbname "tests"})
(def seller "eyJzdWIiOiJhMSIsICJuYW1lIjoiVGVzdCIsICJ1X3R5cCI6IjAifQo=")
(def buyer "eyJzdWIiOiJhMiIsICJuYW1lIjoiQnV5ZXIiLCAidV90eXAiOiIwIn0K")
(def ds (jdbc/get-datasource db-config))
(def db (db-from-ds ds))
(migrate db-config)

(defn- byte-array-read-string [stream]
  (if (some? stream)
    (let [bytes (.readAllBytes stream)
          str (new String bytes StandardCharsets/UTF_8)]
      (if (empty? str) nil (json/read-str str :key-fn keyword)))
    nil))
(defn- request
  ([method token uri]
   (request method token uri nil))
  ([method token uri body]
   (-> ((app-routes db)
        (merge {:uri            uri
                :request-method method
                :scheme         "https"

                :body-params    body}
               (if (some? token) {:headers  {"x-jwt-payload" token}} nil)))
       (select-keys [:status :body])
       (update :body byte-array-read-string))))
(def sample-auction {:title "auction"
                     :startsAt "2023-03-15T11:50:55Z"
                     :expiry "2023-03-16T11:50:55Z"
                     :currency "SEK"})

(deftest test-resource
  (let [auction-id (-> (request :post seller "/auctions" sample-auction) :body :id)
        expected-auction (merge sample-auction {:id 1 :url "https://localhost/auctions/1" :seller "a1"})]
    (is (= 1
           auction-id))
    (is (= {:status 200 :body expected-auction}
           (request :get seller (str "/auctions/" auction-id))))
    ; TODO:
    ;(is (= {:status 404 :body nil}
    ;       (request :get seller (str "/auctions/" 99))))
    (is (= {:status 403 :body {:cause "not-authorized"}}
           (request :post nil "/auctions" sample-auction)))
    (is (= {:status 200 :body [expected-auction]}
           (request :get buyer "/auctions")))))