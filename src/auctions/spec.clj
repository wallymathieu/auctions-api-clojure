(ns auctions.spec
  (:require [malli.core :as m]))
(def non-empty-string
  (m/schema [:string {:min 1}]))
(def date-regex  #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}")
(def date-schema (m/schema [:re date-regex]))
(def auction-id-schema (m/schema :int))

(def currency-schema (m/schema [:enum "VAC" "SEK" "DKK"]))
(def user-regex #"\w*\|[^|]*\|.*?")
(def amount-regex #"[A-Z]+[0-9]+")
(def user-schema
  (m/schema [:re user-regex]))
(def amount-schema
  (m/schema [:re user-regex]))

(def auction-schema
  (m/schema [:map
             [:id {:optional true} auction-id-schema]
             [:title non-empty-string]
             [:startsAt date-schema]
             [:expiry date-schema]
             [:user user-schema]
             [:currencyCode currency-schema]]))

