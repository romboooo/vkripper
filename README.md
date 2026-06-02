# blps

Multi-module Spring Boot project:

- `common` - shared contracts and shared payment/order persistence model.
- `vkripper` - internet shop application: REST API, auth, products, favorites, reviews, cart, purchase creation, Quartz, OpenAPI, JPA/JTA, MQTT publishing.
- `banking-node` - separate payment node: JMS listener, payment processing service, `BankEisClient` JCA extension point, payment/order status updates.

## Build

```bash
./gradlew clean build
./gradlew :common:build
./gradlew :vkripper:build
./gradlew :banking-node:build
```

WAR artifacts:

```text
vkripper/build/libs/vkripper.war
banking-node/build/libs/banking-node.war
```

## Run locally

```bash
./gradlew :vkripper:bootRun
./gradlew :banking-node:bootRun
```

Default application ports:

```text
vkripper:      http://localhost:8080
banking-node: http://localhost:8180
```

## Messaging

`vkripper` creates a purchase order and pending payment, then publishes `PaymentRequestedEvent` to MQTT topic `payment.requested`.

`banking-node` listens to JMS destination `payment.requested`, deserializes the same event from `common`, processes payment through `BankEisClient`, and updates payment/order statuses.

Main app MQTT properties:

```properties
app.mqtt.broker-url=tcp://localhost:1883
app.mqtt.client-id=vkripper
app.mqtt.payment-topic=payment.requested
app.mqtt.username=
app.mqtt.password=
```

Banking node JMS properties:

```properties
app.jms.broker-url=tcp://localhost:61616
app.jms.payment-destination=payment.requested
app.jms.username=
app.jms.password=
```

The current `BankEisClientStub` is a placeholder for the future JCA resource adapter integration.
