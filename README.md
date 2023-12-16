# Auctions in Clojure

It also offers a self-hosted OpenAPI documentation, accessible via Swagger UI.

It persists auctions to Postgres via [next.jdbc](https://github.com/seancorfield/next-jdbc).

# Run on localhost

## Configure PostgreSQL server
First you must run a Postgres server with Docker for eg.:

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

# License & Copyright

Copyright (c) 2023 Oskar Gewalli.
Distributed under the Apache Source License 2.0.
