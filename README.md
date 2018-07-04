# gc-subscriptions

This is a simple demo application intended to show Axon Framework's 3.3 new subscription query functionality.

It is based on the simple GiftCard demo domain that we've used in other examples: giftcard's get issues at a 
certain value and can be redeemed until their depleted. We have a small GUI that allows us to initiate both
operations (command and issue) and shows a table of giftcards that have been issued and their current balance.
We'll be using the subscription query functionality to push updates about giftcards to that table.  

Tech stack used:
* [Axon Framework 3.3](https://axoniq.io/product-overview/axon-framework)
* Java 8
* Spring Boot 2
* JPA, H2
* [Vaadin 8.4](http://vaadin.com)
* [Lombok](https://projectlombok.org)
