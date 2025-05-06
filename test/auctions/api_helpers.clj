(ns auctions.api-helpers
  (:require [auctions.core :refer [app-routes]]
            [clojure.data.json :as json])
  (:import java.nio.charset.StandardCharsets))

(defn- byte-array-read-string [stream]
  (cond
    ; If it is a string, read as json
    (string? stream)
    (when-not (empty? stream) (json/read-str str :key-fn keyword))
    ; If it is a stream, convert it to a string then read as json
    (some? stream)
    (let [bytes (.readAllBytes stream)
          str (new String bytes StandardCharsets/UTF_8)]
      (when-not (empty? str) (json/read-str str :key-fn keyword))) 
    ; it is nil
    :else nil))

(defn request
  ([db method token uri]
   (request db method token uri nil))
  ([db method token uri body]
   (-> ((app-routes db)
        (merge {:uri            uri
                :request-method method
                :scheme         "https"
                :body-params    body}
               (when (some? token) {:headers {"x-jwt-payload" token}})))
       (select-keys [:status :body])
       (update :body byte-array-read-string))))
