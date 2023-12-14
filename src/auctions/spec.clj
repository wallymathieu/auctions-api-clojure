(ns auctions.spec
  (:require [malli.core :as m]))
(comment 
  ;type CurrencyCode =
  ;         /// virtual auction currency
  ;         |VAC=1001
  ;         /// Swedish 'Krona'
  ;         |SEK=752
  ;         /// Danish 'Krone'
  ;         |DKK=208
  )
(def user-regex #"\w*\|[^|]*\|.*?")

(def currency-code string?)

(s/def :auctions/user-type (s/and string? #(re-matches user-regex %)))

(comment
    ;static member TryParse user =
    ;let m = User.Regex.Match(user)
    ;if m.Success then
    ;  match (m.Groups.["type"].Value, m.Groups.["id"].Value, m.Groups.["name"].Value) with
    ;  | "BuyerOrSeller", id, name -> Some (BuyerOrSeller(UserId id, name))
    ;  | "Support", id, _ -> Some (Support(UserId id))
    ;  | type', _, _ -> None
    ;else None
  )
(def amount-regex #"[A-Z]+[0-9]+")
(s/def :auctions/amount-type (s/and string? #(re-matches amount-regex %)))

(comment "
  type TimedAscendingOptions = {
      /// the seller has set a minimum sale price in advance (the 'reserve' price)
      /// and the final bid does not reach that price the item remains unsold
      /// If the reserve price is 0, that is the equivalent of not setting it.
      reservePrice: Amount
      /// Sometimes the auctioneer sets a minimum amount by which the next bid must exceed the current highest bid.
      /// Having min raise equal to 0 is the equivalent of not setting it.
      minRaise: Amount
      /// If no competing bidder challenges the standing bid within a given time frame,
      /// the standing bid becomes the winner, and the item is sold to the highest bidder
      /// at a price equal to his or her bid.
      timeFrame: TimeSpan
    }
  type SingleSealedBidOptions =
    /// Sealed first-price auction
    /// In this type of auction all bidders simultaneously submit sealed bids so that no bidder knows the bid of any
    /// other participant. The highest bidder pays the price they submitted.
    /// This type of auction is distinct from the English auction, in that bidders can only submit one bid each.
    |Blind
    /// Also known as a sealed-bid second-price auction.
    /// This is identical to the sealed first-price auction except that the winning bidder pays the second-highest bid
    /// rather than his or her own
    |Vickrey
      

type Auction =
  { ...
    typ : Type
  }


      ")

(def date-type #(instance? java.time.LocalDateTime %))
(s/def :auctions/id int?)
(s/def :auctions/title string?)
(s/def :auctions/starts-at date-type)
(s/def :auctions/expiry date-type)
(s/def :auctions/at date-type)
(s/def :auctions/amount int?)
(s/def :auctions/user :auctions/user-type)
(s/def :auctions/auction (s/keys
                            :req [:auctions/title
                                     :auctions/starts-at 
                                     :auctions/expiry 
                                     :auctions/user 
                                     :auctions/currency-code]
                            :opt [:auctions/id]))
; if we want to use 'id' for auctions and for bids we need two separate keys, i.e. :auctions/id , :bids/id
(s/def :auctions/bid (s/keys 
                            :req [:auctions/amount :auctions/user]
                            :opt []))
