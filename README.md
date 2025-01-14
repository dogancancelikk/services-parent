# Run Instructions

## Introduction

This repository involves:

1. Data-Source sending random data via Socket to the `Filter-Service`.
2. Filter-Service evaluating each message, writing those to Kafka or file.
3. Two Kafka consumers:
    - Hash-Track stores messages in MongoDB.
    - Storage-Service writes messages into PostgreSQL.

A Docker Compose file is provided to run Kafka, MongoDB, Redis, and PostgreSQL containers.

---

## 1. Prerequisites

- Java 21
- Maven
- Docker

---

## Run Docker Compose

   ```bash
   docker-compose up -d
   ```

## Run Each Microservice

After Kafka, Mongo, Redis and Postgresql are running, you can start each Spring Boot microservice

Filter Service (Socket Broker and Kafka Producer)

  ```bash
   cd filter-service
   mvn spring-boot:run
   ```

Data-Source

```bash
   cd data-source
   mvn spring-boot:run
   ```

Consumer (Mongo)

```bash
   cd hash-track
   mvn spring-boot:run
   ```

Consumer (Postgresql)

```bash
   cd storage-service
   mvn spring-boot:run
   ```
   