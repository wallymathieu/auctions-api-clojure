(ns auctions.migration
  (:require [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]))

(defn- migration-config [config]
  {:datastore (jdbc/sql-database config)
   :migrations (jdbc/load-resources "migrations")})

(defn migrate [config]
  (repl/migrate (migration-config config)))
