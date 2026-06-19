(ns auctions.spec
  (:require [malli.core :as m])
  (:import java.time.Instant))

(def ^:private non-empty-string (m/schema [:string {:min 1}]))

(def DateTime
  (m/schema [:and :string
             [:fn {:error/message "must be a valid ISO-8601 instant"}
              #(try (Instant/parse %) true (catch Exception _ false))]]))


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

(def ErrorResponse
  (m/schema [:map
             [:type non-empty-string]]))

(def AuctionErrorResponse
  (m/schema [:map
             [:type non-empty-string]
             [:auctionId AuctionId]]))

(def BidErrorResponse
  (m/schema [:map
             [:type non-empty-string]
             [:auctionId AuctionId]
             [:amount {:optional true} :int]]))

(def AuctionAddedResponse
  (m/schema [:map
             [:$type non-empty-string]
             [:at non-empty-string]
             [:auction AuctionResult]]))

(def BidAcceptedResponse
  (m/schema [:map
             [:$type non-empty-string]
             [:at non-empty-string]
             [:bid [:map
                    [:auction AuctionId]
                    [:amount :int]
                    [:bidder non-empty-string]]]]))
