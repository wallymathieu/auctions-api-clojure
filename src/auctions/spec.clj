(ns auctions.spec
  (:require [malli.core :as m]))

(def ^:private non-empty-string (m/schema [:string {:min 1}]))

(def ^:private date-regex  #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z?") ;2023-03-15T11:50:55Z

(def DateTime (m/schema  [:re date-regex]))

(def AuctionId (m/schema :int))

(def Currency (m/schema [:enum "VAC" "SEK" "DKK"]))

(def Bid
  (m/schema [:map
             [:amount :int]]))

(def BidResult
  (m/schema [:map
             [:amount :int]
             [:bidder non-empty-string]]))

(def ^:private base-auction-parts
  [[:title non-empty-string]
   [:startsAt DateTime]
   [:expiry DateTime]
   [:currency Currency]
   [:reservePrice {:optional true} :int]
   [:minRaise {:optional true} :int]])

(def Auction
  (m/schema (into [:map]
                  base-auction-parts)))

(def AuctionResult
  (m/schema (into [:map
                   [:seller non-empty-string]
                   [:id {:optional true} AuctionId]
                   [:url {:optional true} :string]
                   [:bids {:optional true} [:vector BidResult]]]
                  base-auction-parts)))

(def ListOfAuctions (m/schema [:vector AuctionResult]))
