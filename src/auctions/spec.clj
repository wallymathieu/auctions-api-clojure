(ns auctions.spec
  (:require [malli.core :as m]))
(def non-empty-string
  (m/schema [:string {:min 1}]))
(def date-regex  #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z") ;2023-03-15T11:50:55Z
(def date-schema (m/schema [:re date-regex]))
(def auction-id-schema (m/schema :int))

(def currency-schema (m/schema [:enum "VAC" "SEK" "DKK"]))
;(def user-regex #"\w*\|[^|]*\|.*?")
(def amount-regex #"[A-Z]+[0-9]+")
;(def user-schema
;  (m/schema [:re user-regex]))
(def amount-schema
  (m/schema [:re amount-regex]))
(def bid-schema
  (m/schema [:map
             [:amount amount-schema]
             [:bidder non-empty-string]]))
(def base-auction-parts
  [[:title non-empty-string]
   [:startsAt date-schema]
   [:expiry date-schema]
   [:seller non-empty-string]
   [:currency currency-schema]])
(def create-auction-schema
  (m/schema [ :map
             [:title non-empty-string]
             [:startsAt date-schema]
             [:expiry date-schema]
             [:seller non-empty-string]
             [:currency currency-schema]]))

(def auction-schema
  (m/schema [:map
             [:id {:optional true} auction-id-schema]
             [:url {:optional true} :string]
             [:bids {:optional true} [:vector bid-schema]]
             [:title non-empty-string]
             [:startsAt date-schema]
             [:expiry date-schema]
             [:seller non-empty-string]
             [:currency currency-schema]]))
(def list-of-auctions-schema (m/schema [:vector auction-schema]))


