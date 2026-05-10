(ns auctions.spec
  (:require [malli.core :as m]))

(def ^:private non-empty-string (m/schema [:string {:min 1}]))

(def ^:private date-regex  #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{3})?Z")

(def DateTime (m/schema  [:re date-regex]))

(def AuctionId (m/schema integer?))

(def Currency (m/schema [:enum "VAC" "SEK" "DKK"]))

(def Bid
  (m/schema [:map
             [:amount :int]]))

(def BidResult
  (m/schema [:map
             [:amount :int]
             [:bidder non-empty-string]]))

(def ^:private base-auction-parts
  [[:id AuctionId]
   [:title non-empty-string]
   [:startsAt DateTime]
   [:endsAt DateTime]
   [:currency Currency]
   [:open :boolean]
   [:reservePrice {:optional true} :int]
   [:minRaise {:optional true} :int]])

(def Auction
  (m/schema (into [:map]
                  base-auction-parts)))

(def AuctionResult
  (m/schema (into [:map
                   [:seller non-empty-string]
                   [:url {:optional true} :string]
                   [:bids {:optional true} [:vector BidResult]]
                   [:winner {:optional true} [:or nil? non-empty-string]]
                   [:winnerPrice {:optional true} [:or nil? :int]]]
                  base-auction-parts)))

(def ListOfAuctions (m/schema [:vector AuctionResult]))

(def ValidationError
  (m/schema [:map
             [:value :any]
             [:in [:vector :any]]
             [:humanized :any]]))
