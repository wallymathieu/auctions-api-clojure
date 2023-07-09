(ns auctions.api-helpers
  (:require [auctions.core :refer [app-routes]]
            [clojure.data.json :as json])
  (:import java.nio.charset.StandardCharsets))
(defn- byte-array-read-string [stream]
  (if (some? stream)
    (let [bytes (.readAllBytes stream)
          str (new String bytes StandardCharsets/UTF_8)]
      (if (empty? str) nil (json/read-str str :key-fn keyword)))
    nil))
(defn request
  ([db method token uri]
   (request db method token uri nil))
  ([db method token uri body]
   (-> ((app-routes db)
        (merge {:uri            uri
                :request-method method
                :scheme         "https"

                :body-params    body}
               (if (some? token) {:headers  {"x-jwt-payload" token}} nil)))
       (select-keys [:status :body])
       (update :body byte-array-read-string))))