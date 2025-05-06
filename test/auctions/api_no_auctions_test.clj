(ns auctions.api-no-auctions-test
  (:require [auctions.api-helpers :refer [request]]
            [auctions.migration :refer [migrate]]
            [auctions.samples :refer [buyer]]
            [clojure.test :refer [deftest is]]
            [next.jdbc :as jdbc]))

(def db-config {:dbtype "h2:mem" :dbname "no-auctions-tests"})

(def db (jdbc/get-datasource db-config))
(migrate db-config)


(deftest test-resource
  (is (= {:status 200 :body []}
         (request db :get buyer "/auctions")))
  (is (= {:status 200 :body []}
         (request db :get nil "/auctions")))
  (is (= {:status 404 :body nil}
         (request db :get nil "/does-not-exist")))
  (is (= {:status 200 :body "file://favicon.svg"}
         (request db :get nil "/favicon.svg"))))
