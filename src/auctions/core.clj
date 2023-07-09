(ns auctions.core
  (:require [auctions.handlers :as auction]
            [auctions.migration :refer [migrate]]
            [auctions.store :as store :refer [db-from-ds jdbc-database-url]]
            [muuntaja.core :as m]
            [next.jdbc :as jdbc]
            [reitit.coercion.schema :as rcs]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as rrc]
            [reitit.ring.middleware.muuntaja :as rrmm]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.cors :refer [wrap-cors]] 
            [schema.core :as s]))


(defn ok [body]
  {:status 200
   :body body})

(defn append-auction-url [auction request]
  (let [host (-> request :headers (get "host" "localhost"))
        scheme (name (:scheme request))
        id (:id auction)]
    (merge auction {:url (str scheme "://" host "/auctions/" id)})))

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
                             :handler (partial auction/list-all-auctions db)}
                   :post    {:summary "Creates a Auction resource."
                             :handler (partial auction/create-auction db)}
                   :delete  {:summary "Removes all Auction resources"
                             :handler (partial auction/delete-all-auctions db)}
                   :options (fn [_] {:status 200})}]
     ["/auctions/:id" {:parameters {:path {:id s/Int}}
                       :get        {:summary "Retrieves a Auction resource."
                                    :handler (partial auction/retrieve-auction db)}
                       :patch      {:summary "Updates the Auction resource."
                                    :handler (fn [{:keys [parameters body-params] :as req}] (-> body-params
                                                                                                (store/update-auction db (get-in parameters [:path :id]))
                                                                                                (append-auction-url req)
                                                                                                ok))}
                       :delete     {:summary "Removes the Auction resource."
                                    :handler (fn [{:keys [parameters]}] (store/delete-auctions db (get-in parameters [:path :id]))
                                               {:status 204})}}]]
    {:data {:muuntaja   m/instance
            :coercion   rcs/coercion
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
