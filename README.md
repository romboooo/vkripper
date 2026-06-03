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

`vkripper` creates a purchase order and pending payment, then publishes `PaymentRequestedEvent` to MQTT topic `VirtualTopic/payment/requested`.

`banking-node` listens to JMS queue `Consumer.banking.VirtualTopic.payment.requested`, deserializes the same event from `common`, processes payment through `BankEisClient`, transfers buyer/seller balances, and updates payment/order statuses.

The `VirtualTopic` naming pattern keeps MQTT publishing simple while the JMS side consumes from a real queue.

Main app MQTT properties:

```properties
app.mqtt.broker-url=tcp://localhost:1883
app.mqtt.client-id=vkripper
app.mqtt.payment-topic=VirtualTopic/payment/requested
app.mqtt.username=admin
app.mqtt.password=admin
```

Banking node JMS properties:

```properties
app.jms.broker-url=tcp://localhost:61616
app.jms.payment-destination=Consumer.banking.VirtualTopic.payment.requested
app.jms.username=admin
app.jms.password=admin
```

ActiveMQ Classic users for the Helios deployment:

```text
admin/admin
rmb/rmb
shmi/shmi
```

Helios deployment:

```text
ActiveMQ Classic: ~/blps/activemq/apache-activemq-5.18.5
WildFly deployments: ~/blps/wildfly-39.0.1.Final/standalone/deployments
Start script: ~/blps/start.sh
Main app through SSH alias ports: http://localhost:17272/vkripper
ActiveMQ console through SSH alias apacheports: http://localhost:8161/admin/
```

The current `BankEisClientStub` is a placeholder for the future JCA resource adapter integration.
