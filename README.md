# Using Vault in Spring applications

These assets demonstrate how to use Vault in Spring applications.

Each subdirectory includes assets to do the following:

- `vault-transit/`: Encrypt and decrypt application payloads with Vault transit secrets engine


### Setup

1. Install [Docker Compose](https://docs.docker.com/compose/install/).

1. Run Docker Compose to set up application dependencies.

   ```shell
   $ docker compose up -d
   ```

1. Run the application using Maven Wrapper.

   ```shell
   $ ./mvnw spring-boot:run
   ```


### Sample Application

Each project uses a sample application with the following API:

- Get payments

```
$ curl localhost:8080/payments

[{"cc_info":"4242424242424242","id":"b7703d9a-ff7b-4898-91ba-4b5adba8b7e5","name":"Test Customer","createdAt":"2024-05-09T17:51:21.099933Z"}]
```

- Create a payment

```
$ curl -XPOST -d '{"name": "Test Customer", "cc_info": "4242424242424242"}' -H 'Content-Type:application/json' localhost:8080/payments

[{"cc_info":"4242424242424242","id":"b7703d9a-ff7b-4898-91ba-4b5adba8b7e5","name":"Test Customer","createdAt":"2024-05-09T17:51:21.099933Z"}]
```

The application requires the following dependencies:

- Vault server
- PostgreSQL database