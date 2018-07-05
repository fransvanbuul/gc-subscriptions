# gc-subscriptions

This is a simple demo application intended to show Axon Framework's 3.3 new subscription query functionality.

It is based on the simple GiftCard demo domain that we've used in other examples: gift cards get issued at a 
certain value and can be redeemed until they're depleted. We have a small GUI that allows us to initiate both
operations (command and issue) and shows a table of gift cards that have been issued and their current balance.
We'll be using the subscription query functionality to push updates about gift cards to that table.

Tech stack used:
* [Axon Framework 3.3](https://axoniq.io/product-overview/axon-framework)
* [Reactor Core 3.1](https://projectreactor.io/) (required for Axon subscription queries)
* Java 8
* [Spring Boot 2](https://spring.io/projects/spring-boot)
* JPA/Hibernate, H2
* [Vaadin 8.4](https://vaadin.com), with [Server Push](https://vaadin.com/docs/v8/framework/advanced/advanced-push.html) to push query updates to the browser.
* [Lombok](https://projectlombok.org)

The application can simply be run (e.g. "mvn spring-boot:run") and accessed on http://localhost:8080 . There are no external dependencies.
