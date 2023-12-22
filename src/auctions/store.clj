(ns auctions.store
  (:require [clojure.set :refer [rename-keys]]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]))

(def jdbc-database-url (System/getenv "JDBC_DATABASE_URL"))

(def ^:private db-options {:builder-fn rs/as-unqualified-lower-maps})

(defn db-from-ds [ds] (jdbc/with-options ds db-options))

(defn- as-row [row]
  (rename-keys row {:order :position, :expiry :endsAt}))

(defn- as-auction [row]
  (dissoc (rename-keys row {:position :order, :startsat :startsAt, :endsat :expiry}) :timeframe :minraise :reserveprice))

(defn- as-bid [row]
  (dissoc (rename-keys row {:position :order}) :at :id :auctionid))


(defn- map-auction-with-bids [bids-for-auction]
  (fn [{:keys [id] :as auction}] (merge auction {:bids (map as-bid (bids-for-auction id))})))

(defn create-auction [db auction]
  (as-auction (sql/insert! db :auctions (as-row auction))))

(defn- get-auctions-sql [db auction-sql-params bids-sql-params]
  (let [auctions (jdbc/execute! db auction-sql-params db-options)
        bids (jdbc/execute! db bids-sql-params db-options)
        mapped-auctions (map as-auction auctions)
        grouped-bids (group-by :auctionid bids)
        bids-for-auction (fn [id] (get grouped-bids id []))]
    (map (map-auction-with-bids bids-for-auction) mapped-auctions)))

(defn get-auction [db id]
  (let [auctions (get-auctions-sql db ["SELECT * FROM auctions WHERE id = ?" id] ["SELECT * FROM bids WHERE auctionId = ?" id])]
    (first auctions)))


(defn add-bid [db body id]
  (jdbc/with-transaction [tx db]
    (let [auction (get-auction tx id)]
      (when (some? auction)
        (sql/insert! tx :bids (merge (as-row body) {:auctionId id}))
        nil
        (get-auction tx id)))))

(defn get-all-auctions [db]
  (get-auctions-sql db ["SELECT * FROM auctions"] ["SELECT * FROM bids"]))

