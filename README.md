# blps

Multi-module Spring Boot project:

- `common` - shared contracts and shared finance/payment/order persistence model.
- `vkripper` - internet shop application: REST API, auth, products, favorites, reviews, cart, purchase creation, Quartz, OpenAPI, JPA/JTA, MQTT publishing.
- `finance-node` - separate finance node: JMS listener, financial operation processing service, `BankEisClient` JCA extension point, balance/payment/order status updates.

## Build

```bash
./gradlew clean build
./gradlew :common:build
./gradlew :vkripper:build
./gradlew :finance-node:build
```

WAR artifacts:

```text
vkripper/build/libs/vkripper.war
finance-node/build/libs/finance-node.war
```

## Run locally

```bash
./gradlew :vkripper:bootRun
./gradlew :finance-node:bootRun
```

Default application ports:

```text
vkripper:      http://localhost:8080
finance-node: http://localhost:8180
```

## Messaging

`vkripper` creates pending financial operations for top-up, withdrawal, and internal-balance purchases, then publishes `FinancialOperationRequestedEvent` to MQTT topic `VirtualTopic/financial-operation/requested`.

`finance-node` listens to JMS queue `Consumer.finance.VirtualTopic.financial-operation.requested`, deserializes the same event from `common`, simulates asynchronous bank/ledger processing through `BankEisClient`, updates balances, and updates financial operation/payment/order statuses.

The `VirtualTopic` naming pattern keeps MQTT publishing simple while the JMS side consumes from a real queue.

Main app MQTT properties:

```properties
app.mqtt.broker-url=tcp://localhost:1883
app.mqtt.client-id=vkripper
app.mqtt.financial-operation-topic=VirtualTopic/financial-operation/requested
app.mqtt.username=admin
app.mqtt.password=admin
```

Finance node JMS properties:

```properties
app.jms.broker-url=tcp://localhost:61616
app.jms.financial-operation-destination=Consumer.finance.VirtualTopic.financial-operation.requested
app.jms.username=admin
app.jms.password=admin
app.finance.processing-delay=3s
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
