(ns auctions.store
  (:require [clojure.set :refer [rename-keys]]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]))
(def jdbc-database-url (System/getenv "JDBC_DATABASE_URL"))

(defn db-from-ds [ds] (jdbc/with-options ds {:builder-fn rs/as-unqualified-lower-maps}))

(defn as-row [row]
  (rename-keys row {:order :position, :expiry :endsAt}))

(defn as-auction [row]
  (dissoc (rename-keys row {:position :order, :startsat :startsAt, :endsat :expiry}) :timeframe :minraise :reserveprice))

(defn as-bid [row]
  (dissoc (rename-keys row {:position :order}) :at :id :auctionid))


(defn- map-auction-with-bids [bids-for-auction]
  (fn [{:keys [id] :as auction}] (merge auction {:bids (map as-bid (bids-for-auction id))})))

(defn create-auctions [db auction]
  (as-auction (sql/insert! db :auctions (as-row auction))))

(defn- get-auctions-sql [db auction-sql-params bids-sql-params]
  (let [auctions (jdbc/execute! db auction-sql-params)
        bids (jdbc/execute! db bids-sql-params)
        mapped-auctions (map as-auction auctions)
        grouped-bids (group-by :auctionid bids)
        bids-for-auction (fn [id] (get grouped-bids id []))]
    (map (map-auction-with-bids bids-for-auction) mapped-auctions)))

(defn get-auction [db id]
  (let [auctions (get-auctions-sql db ["SELECT * FROM auctions WHERE id = ?" id] [ "SELECT * FROM bids WHERE auctionId = ?" id])]
    (first auctions)))

(defn update-auction [db body id]
  (sql/update! db :auctions (as-row body) {:id id})
  (get-auction db id))

(defn add-bid [db body id]
  (let [auction-id (-> (get-auction db id) :id)]
    (if-not (nil? auction-id)
      (do
        (sql/insert! db :bids (merge (as-row body) {:auctionId id}))
        (get-auction db id))
      nil)))

(defn get-all-auctions [db]
  (get-auctions-sql db ["SELECT * FROM auctions"] ["SELECT * FROM bids"]))

