(ns auctions.handlers
  (:require [ring.util.response :as rr]
            [clojure.data.json :as json]
            [auctions.store :as store])
  (:import [java.util Base64]
           [java.time Instant LocalDateTime ZoneOffset]
           [java.sql Timestamp]))

(defn decode64 [to-decode]
  (try
    (when (some? to-decode)
      (String. (.decode (Base64/getMimeDecoder) to-decode)))
    (catch Exception _ nil)))

(defn- error-response [status body]
  {:status status :body body})

; TODO: move to middleware
;"sub" "name" "u_typ"
(defn if-authorized [req callback]
  (let [auth-header (-> req :headers (get "x-jwt-payload"))
        auth-json (decode64 auth-header)
        decoded (when (some? auth-json) (json/read-str auth-json))]
    (cond
      (or (not auth-header) (not decoded))
      (error-response 401 {:type "NotAuthorized"})
      :else
      (callback decoded))))

(defn- instant-value [timestamp]
  (cond
    (nil? timestamp) nil
    (instance? Instant timestamp) timestamp
    (instance? LocalDateTime timestamp) (.toInstant ^LocalDateTime timestamp ZoneOffset/UTC)
    (instance? Timestamp timestamp) (.toInstant ^Timestamp timestamp)
    (string? timestamp) (Instant/parse timestamp)
    :else nil))

(defn- timestamp-to-string [timestamp]
  (some-> timestamp instant-value str))

(defn- nil-response-if-not-found [auction]
  (if (some? auction)
    (rr/response auction)
    (rr/not-found nil)))

(defn- now []
  (Instant/now))

(defn- ended? [auction now-instant]
  (let [ends-at (instant-value (:endsAt auction))]
    (and (some? ends-at) (not (.isAfter ends-at now-instant)))))

(defn- started? [auction now-instant]
  (let [starts-at (instant-value (:startsAt auction))]
    (and (some? starts-at) (not (.isAfter starts-at now-instant)))))

(defn- enrich-with-winner [db {:keys [id] :as auction}]
  (let [winner-bid (store/get-auction-winning-bid db id)]
    (if (ended? auction (now))
      (assoc auction
             :winner (:bidder winner-bid)
             :winnerPrice (:amount winner-bid))
      (assoc auction
             :winner nil
             :winnerPrice nil))))

(defn- append-auction-url-and-convert-timestamps [auction request]
  (let [host (-> request :headers (get "host" "localhost"))
        scheme (name (:scheme request))
        id (:id auction)
        startsAt (:startsAt auction)
        endsAt (:endsAt auction)]
    (when (some? id)
      (merge auction {:url (str scheme "://" host "/auctions/" id)
                      :startsAt (timestamp-to-string startsAt)
                      :endsAt (timestamp-to-string endsAt)}))))

(defn list-all-auctions [db request]
  (->> (store/get-all-auctions db)
       (map #(enrich-with-winner db %))
       (mapv #(append-auction-url-and-convert-timestamps % request))
       rr/response))

(defn create-auction [db {:keys [body-params] :as request}]
  (if-authorized request
                 (fn [user]
                   (let [auction-id (:id body-params)
                         auction-with-user (merge body-params {:seller (get user "sub")})]
                     (cond
                       (ended? body-params (now))
                       (error-response 400 {:type "AuctionHasEnded"
                                            :auctionId auction-id})

                       (some? (store/get-auction db auction-id))
                       (error-response 400 {:type "AuctionAlreadyExists"
                                            :auctionId auction-id})

                       :else
                       (let [created-auction (store/create-auction db auction-with-user)]
                         (rr/response {:$type "AuctionAdded"
                                       :at (str (now))
                                       :auction (append-auction-url-and-convert-timestamps
                                                 (enrich-with-winner db created-auction)
                                                 request)})))))))

(defn retrieve-auction [db {:keys [parameters] :as request}]
  (let [id (-> parameters :path :id)]
    (-> (store/get-auction db id)
        (some->> (enrich-with-winner db))
        (append-auction-url-and-convert-timestamps request)
        nil-response-if-not-found)))

(defn add-bid-to-auction [db {:keys [parameters, body-params] :as request}]
  (if-authorized request
                 (fn [user]
                   (let [id (-> parameters :path :id)
                         auction (store/get-auction db id)
                         bid-with-user (merge body-params {:bidder (get user "sub")
                                                           :at (LocalDateTime/now)})
                         amount (get bid-with-user :amount)
                         highest-bid (store/get-auction-winning-bid db id)
                         highest-amount (if (some? highest-bid) (get highest-bid :amount) 0)]
                     (cond
                       (nil? auction)
                       (error-response 404 {:type "AuctionNotFound"
                                            :auctionId id})

                       (= (:seller auction) (get user "sub"))
                       (error-response 400 {:type "SellerCannotPlaceBids"
                                            :auctionId id})

                       (not (started? auction (now)))
                       (error-response 400 {:type "AuctionHasNotStarted"
                                            :auctionId id})

                       (ended? auction (now))
                       (error-response 400 {:type "AuctionHasEnded"
                                            :auctionId id})

                       (<= amount highest-amount)
                       (error-response 400 {:type "MustPlaceBidOverHighestBid"
                                            :amount highest-amount
                                            :auctionId id})

                       :else
                       (do
                         (store/add-bid db bid-with-user id)
                         (rr/response {:$type "BidAccepted"
                                       :at (str (now))
                                       :bid {:auction id
                                             :amount amount
                                             :bidder (get user "sub")}})))))))

