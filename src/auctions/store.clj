(ns auctions.store
  (:require [clojure.set :refer [rename-keys]]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql])
  (:import [java.sql Timestamp]
           [java.time Instant]))

(def jdbc-database-url (System/getenv "JDBC_DATABASE_URL"))

(def ^:private db-options {:builder-fn rs/as-unqualified-lower-maps})

(defn- parse-timestamp [v]
  (when (string? v)
    (Timestamp/from (Instant/parse v))))

(defn- as-row [row]
  (cond-> (rename-keys row {:order :position})
    (string? (:startsAt row)) (update :startsAt parse-timestamp)
    (string? (:endsAt row))   (update :endsAt parse-timestamp)))

(defn- as-auction [row]
  (dissoc (rename-keys row {:position :order, :startsat :startsAt, :endsat :endsAt, :winnerprice :winnerPrice}) :timeframe :minraise :reserveprice))

(defn- as-bid [row]
  (dissoc (rename-keys row {:position :order}) :at :id :auctionid))

(defn- map-auction-with-bids [bids-for-auction]
  (fn [{:keys [id] :as auction}] (merge auction {:bids (mapv as-bid (bids-for-auction id))})))

(defn- get-auctions-sql [db auction-sql-params bids-sql-params]
  (let [auctions (jdbc/execute! db auction-sql-params db-options)
        bids (jdbc/execute! db bids-sql-params db-options)
        mapped-auctions (map as-auction auctions)
        grouped-bids (group-by :auctionid bids)
        bids-for-auction (fn [id] (get grouped-bids id []))]
    (map (map-auction-with-bids bids-for-auction) mapped-auctions)))

(defn- get-bids-sql [db bids-sql-params]
  (jdbc/execute! db bids-sql-params db-options))

(defn get-auction [db id]
  (let [auctions (get-auctions-sql db ["SELECT * FROM auctions WHERE id = ?" id] ["SELECT * FROM bids WHERE auctionId = ?" id])]
    (first auctions)))

(defn create-auction [db auction]
  ;; H2 only returns the generated id from insert!, while PostgreSQL returns the
  ;; whole row, so re-fetch the auction to get the same shape on both databases.
  (let [inserted (sql/insert! db :auctions (as-row auction) db-options)]
    (get-auction db (or (:id auction) (:id inserted)))))

(defn get-auction-winning-bid [db id]
  (let [bids (get-bids-sql db ["SELECT * FROM bids WHERE auctionId = ? ORDER BY amount DESC" id])]
    (first bids)))

(defn add-bid [db body id]
  (jdbc/with-transaction [tx db]
    (let [auction (get-auction tx id)]
      (when (some? auction)
        (sql/insert! tx :bids (merge (as-row body) {:auctionId id}) db-options)
        (get-auction tx id)))))

(defn get-all-auctions [db]
  (get-auctions-sql db ["SELECT * FROM auctions"] ["SELECT * FROM bids"]))

