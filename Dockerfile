FROM clojure:temurin-21-tools-deps

WORKDIR /app

COPY deps.edn ./
RUN clojure -P

COPY . .

EXPOSE 3000

CMD ["clojure", "-M", "-m", "auctions.core", "3000"]
