(ns auctions.core
  (:require [auctions.handlers :as auction]
            [auctions.migration :refer [migrate]]
            [auctions.spec :refer [Auction AuctionId AuctionResult
                                   Bid ListOfAuctions]]
            [auctions.store :as store :refer [db-from-ds jdbc-database-url]]
            [muuntaja.core :as m]
            [next.jdbc :as jdbc]
            [reitit.coercion.malli :as rcm]
            [malli.util :as mu]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as rrc]
            [reitit.ring.middleware.muuntaja :as rrmm]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.cors :refer [wrap-cors]]))


(defn app-routes [db]
  (ring/ring-handler
   (ring/router
    [["/swagger.json" {:get
                       {:no-doc  true
                        :swagger
                        {:basePath "/"
                         :info     {:title       "auctions API"
                                    :description "This is a implementation of the auctions API REST, using Clojure, Ring/Reitit and next-jdbc."
                                    :version     "1.0.0"}}
                        :handler (swagger/create-swagger-handler)}}]
     ["/auctions" {:get     {:summary "Retrieves the collection of Auction resources."
                             :responses {200 {:body ListOfAuctions}}
                             :handler (partial auction/list-all-auctions db)}
                   :post    {:summary "Creates a Auction resource."
                             :parameters {:body Auction}
                             :handler (partial auction/create-auction db)}
                   :options (fn [_] {:status 200})}]
     ["/auctions/:id" {:parameters {:path {:id AuctionId}}
                       :get        {:summary "Retrieves a Auction resource."
                                    :responses {200 {:body AuctionResult} 
                                                404 {:body nil}}
                                    :handler (partial auction/retrieve-auction db)}}]
     ["/auctions/:id/bids" {:parameters {:body Bid
                                         :path {:id AuctionId}}
                            :post        {:summary "Add bid to auction resource."
                                          :responses {200 {:body AuctionResult}
                                                      400 {:body nil}
                                                      404 {:body nil}
                                                      }
                                          :handler (partial auction/add-bid-to-auction db)}}]]
    {:data {:muuntaja   m/instance
            :coercion   (rcm/create
                         {:transformers {:body {:default rcm/default-transformer-provider
                                                :formats {"application/json" rcm/json-transformer-provider}}
                                         :string {:default rcm/string-transformer-provider}
                                         :response {:default rcm/default-transformer-provider}}
                           ;; set of keys to include in error messages
                          :error-keys #{ #_:type #_:coercion :in #_:schema :value #_:errors :humanized #_:transformed}
                           ;; support lite syntax?
                          :lite true
                           ;; schema identity function (default: close all map schemas)
                          :compile mu/closed-schema
                           ;; validate request & response
                          :validate true
                           ;; top-level short-circuit to disable request & response coercion
                          :enabled true
                           ;; strip-extra-keys (affects only predefined transformers)
                          :strip-extra-keys true
                           ;; add/set default values
                          :default-values true
                           ;; encode-error
                          :encode-error nil
                           ;; malli options
                          :options nil})
            :middleware [rrmm/format-middleware
                         rrc/coerce-exceptions-middleware
                         rrc/coerce-response-middleware
                         rrc/coerce-request-middleware
                         [wrap-cors :access-control-allow-origin  #".*"
                          :access-control-allow-methods [:get :put :post :patch :delete]]]}})
   (ring/routes
    (swagger-ui/create-swagger-ui-handler {:path "/"}))
   (ring/create-default-handler
    {:not-found (constantly {:status 404 :body "Not found"})})))

(defn -main [port]
  (let [ds (jdbc/get-datasource jdbc-database-url)
        db (db-from-ds ds)
        routes (#'app-routes db)]
    (migrate jdbc-database-url)
    (jetty/run-jetty routes {:port (Integer. port)
                             :join? false})))

(comment
  (def server
    (let [ds (jdbc/get-datasource jdbc-database-url)
          db (db-from-ds ds)
          routes (#'app-routes db)]
      (jetty/run-jetty #'routes {:port 3000
                                 :join? false}))))
