# Auctions in Clojure

It also offers a self-hosted OpenAPI documentation, accessible via Swagger UI.

It persists auctions to Postgres via [next.jdbc](https://github.com/seancorfield/next-jdbc).

# Run with Docker Compose

The simplest way to run the app and Postgres together:

```
$ docker compose up
```

The API is then available at http://localhost:8080 (the app inside the container listens on 3000; the compose file maps it to 8080 on the host). Source is bind-mounted into the container, so editing files and restarting (`docker compose restart app`) picks up changes.

To stop and remove the containers (keeps the database volume):

```
$ docker compose down
```

### Podman + docker-compose note

If you're using `docker-compose` on top of Podman 3.x and see `CNI network "auctions-api-clojure_default" not found`, pre-create the network once:

```
$ podman network create auctions-api-clojure_default
```

# Run on localhost

## Configure PostgreSQL server
You can start just the database with compose:

```
$ docker compose up -d db
```

Or run Postgres manually:

```
$ docker run --name some-postgres -e POSTGRES_DB=auctions -e POSTGRES_PASSWORD=mypass -d -p 5432:5432 postgres
```

## Run the application

```
$ export JDBC_DATABASE_URL="jdbc:postgresql://localhost/auctions?user=postgres&password=mypass"
$ clj -M -m auctions.core 3000
```

If that port is in use, start it on a different port. For example, port 8100:

```
$ clj -m auctions.core 8100
```

## Updates

In order to check for updates you can use: [antq](https://github.com/liquidz/antq)

To install dependencies you can use `clojure -X:deps prep`
# License & Copyright

Copyright (c) 2023 Oskar Gewalli.
Distributed under the Apache Source License 2.0.
