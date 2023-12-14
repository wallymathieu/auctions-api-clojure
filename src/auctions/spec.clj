(ns auctions.spec
  (:require [malli.core :as m]))

(def auction-schema
  (m/schema
   [:map
    [:title :string]
    [:startsAt :string]
    [:expiry :string ]
    [:user :string]
    [:currencyCode :string]]))

