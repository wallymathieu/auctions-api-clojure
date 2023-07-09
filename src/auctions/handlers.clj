(ns auctions.handlers
  (:require [ring.util.response :as rr]
            [clojure.data.json :as json]
            [auctions.store :as store]))

(defn decode64 [to-decode]
  (if (some? to-decode)
    (String. (.decode (java.util.Base64/getMimeDecoder) to-decode))
    nil))
;"sub" "name" "u_typ"
(defn if-authorized [req callback]
  (let [auth-header (-> req :headers (get "x-jwt-payload"))
        auth-json (decode64 auth-header)
        decoded (if (some? auth-json) (json/read-str auth-json) nil)]
    (cond
      (or (not auth-header) (not decoded))
      {:status 403 :body {:cause :not-authorized}}

      :else
      (callback decoded))))
(defn append-auction-url [auction request]
  (let [host (-> request :headers (get "host" "localhost"))
        scheme (name (:scheme request))
        id (:id auction)]
    (merge auction {:url (str scheme "://" host "/auctions/" id)})))

(defn list-all-auctions [db request]
  (if-authorized request
                 (fn [_]
                   (-> #(append-auction-url % request)
                       (map (store/get-all-auctions db))
                       rr/response))))

(defn create-auction [db {:keys [body-params] :as request}]
  (if-authorized request
                 (fn [user]
                   (let [auction-with-user (merge body-params {:seller (get user "sub")})]
                     (-> (store/create-auctions db auction-with-user)
                         (append-auction-url request)
                         rr/response)))))


(defn retrieve-auction [db {:keys [parameters] :as request}]
  (let [id (-> parameters :path :id)]
    (-> (store/get-auction db id)
        (append-auction-url request)
        rr/response)))

(defn add-bid-to-auction [db {:keys [parameters, body-params] :as request}]
  (if-authorized request
               (fn [user]
                 (let [id (-> parameters :path :id)
                       bid-with-user (merge body-params {:bidder (get user "sub") :at (java.time.LocalDateTime/now)})]
                   (-> (store/add-bid db bid-with-user id)
                       (append-auction-url request)
                       rr/response)))))

