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

(defn create-auctions [db auction]
  (as-auction (sql/insert! db :auctions (as-row auction))))

(defn get-auction [db id]
  (as-auction (sql/get-by-id db :auctions id)))

(defn update-auction [db body id]
  (sql/update! db :auctions (as-row body) {:id id})
  (get-auction db id))

(defn delete-auctions [db id]
  (sql/delete! db :auctions {:id id}))

(defn get-all-auctions [db] 
  (let [auctions (jdbc/execute! db ["SELECT * FROM auctions;"])]
   (map as-auction auctions)))

(defn delete-all-auctions [db]
  (sql/delete! db :auctions [true]))
