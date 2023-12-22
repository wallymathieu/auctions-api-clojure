(ns auctions.handlers
  (:require [ring.util.response :as rr]
            [clojure.data.json :as json]
            [auctions.store :as store]
            [auctions.handlers :as auction])
  (:import [java.util Base64]
           [java.time LocalDateTime]))

(defn decode64 [to-decode]
  (when (some? to-decode)
    (String. (.decode (Base64/getMimeDecoder) to-decode))))

; TODO: move to middleware
;"sub" "name" "u_typ"
(defn if-authorized [req callback]
  (let [auth-header (-> req :headers (get "x-jwt-payload"))
        auth-json (decode64 auth-header)
        decoded (when (some? auth-json) (json/read-str auth-json))]
    (cond
      (or (not auth-header) (not decoded))
      {:status 403 :body {:cause :not-authorized}}
      :else
      (callback decoded))))

(defn- timestamp-to-string [timestamp]
  (when (some? timestamp)
    (str (.toInstant  timestamp))))

(defn- nil-response-if-not-found [auction]
  (if (some? auction)
    (rr/response auction)
    (rr/not-found nil)))

(defn- append-auction-url-and-convert-timestamps [auction request]
  (let [host (-> request :headers (get "host" "localhost"))
        scheme (name (:scheme request))
        id (:id auction)
        startsAt (:startsAt auction)
        expiry (:expiry auction)]
    (when (some? id)
      (merge auction {:url (str scheme "://" host "/auctions/" id)
                      :startsAt (timestamp-to-string startsAt)
                      :expiry (timestamp-to-string expiry)}))))

(defn list-all-auctions [db request]
  (if-authorized request
                 (fn [_]
                   (-> #(append-auction-url-and-convert-timestamps % request)
                       (map (store/get-all-auctions db))
                       rr/response))))

(defn create-auction [db {:keys [body-params] :as request}]
  (if-authorized request
                 (fn [user]
                   (let [auction-with-user (merge body-params {:seller (get user "sub")})]
                     (-> (store/create-auction db auction-with-user)
                         (append-auction-url-and-convert-timestamps request)
                         rr/response)))))

(defn retrieve-auction [db {:keys [parameters] :as request}]
  (let [id (-> parameters :path :id)]
    (-> (store/get-auction db id)
        (append-auction-url-and-convert-timestamps request)
        nil-response-if-not-found)))

(defn add-bid-to-auction [db {:keys [parameters, body-params] :as request}]
  (if-authorized request
                 (fn [user]
                   (let [id (-> parameters :path :id)
                         bid-with-user (merge body-params {:bidder (get user "sub")
                                                           :at (LocalDateTime/now)})]
                     (-> (store/add-bid db bid-with-user id)
                         (append-auction-url-and-convert-timestamps request)
                         nil-response-if-not-found)))))

