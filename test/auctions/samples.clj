(ns auctions.samples
  (:import [java.time Instant]
           [java.time.temporal ChronoUnit]))
(def seller "eyJzdWIiOiJhMSIsICJuYW1lIjoiVGVzdCIsICJ1X3R5cCI6IjAifQo=")
(def buyer "eyJzdWIiOiJhMiIsICJuYW1lIjoiQnV5ZXIiLCAidV90eXAiOiIwIn0K")

(defn- instant-str [^Instant instant]
  (str (.truncatedTo instant ChronoUnit/SECONDS)))

;; an auction that has started but not yet ended
(def sample-auction {:id 1
                     :title "auction"
                     :startsAt (instant-str (.minus (Instant/now) 1 ChronoUnit/DAYS))
                     :endsAt (instant-str (.plus (Instant/now) 1 ChronoUnit/DAYS))
                     :currency "SEK"
                     :open true})
