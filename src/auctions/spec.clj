(ns auctions.spec
  (:require [malli.core :as m]
            ;[malli.registry :as mr]
            ;[malli.experimental.time :as met]
            ))
(comment (mr/set-default-registry!
   (mr/composite-registry
    (m/default-schemas)
    (met/schemas))))

(def ^:private non-empty-string
  (m/schema [:string {:min 1}]))
(def ^:private date-regex  #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z?") ;2023-03-15T11:50:55Z
(def DateTime (m/schema  [:re date-regex]))
(def AuctionId (m/schema :int))

(def Currency (m/schema [:enum "VAC" "SEK" "DKK"]))
;(def user-regex #"\w*\|[^|]*\|.*?")
(def ^:private amount-regex #"[A-Z]+[0-9]+")
;(def user-schema
;  (m/schema [:re user-regex]))
(def Amount
  (m/schema [:re amount-regex]))
(def Bid
  (m/schema [:map
             [:amount Amount]
             [:bidder non-empty-string]]))
(def ^:private base-auction-parts
  [[:title non-empty-string]
   [:startsAt DateTime]
   [:expiry DateTime]
   [:seller non-empty-string]
   [:currency Currency]
   [:reservePrice {:optional true} :int]
   [:minRaise {:optional true} :int]])
(def Auction
  (m/schema (into [:map]
                  base-auction-parts)))
(def AuctionResult
  (m/schema (into [:map
                   [:id {:optional true} AuctionId]
                   [:url {:optional true} :string]
                   [:bids {:optional true} [:vector Bid]]]
                  base-auction-parts)))
(def ListOfAuctions (m/schema [:vector AuctionResult]))

