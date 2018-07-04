package com.example.giftcard.command;

import lombok.ToString;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

import java.math.BigDecimal;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
@XSlf4j
@ToString
public class GiftCard {

    @AggregateIdentifier private String id;
    private BigDecimal remainingValue;

    public GiftCard() {
        log.trace("empty constructor invoked");
    }

    @CommandHandler
    public GiftCard(IssueCommand command) {
        log.entry(command);
        if(command.getAmount().signum() <= 0) throw new IllegalArgumentException("amount <= 0");
        apply(new IssuedEvent(command.getId(), command.getAmount()));
    }

    @CommandHandler
    public void handle(RedeemCommand command) {
        log.entry(command);
        if(command.getAmount().signum() <= 0) throw new IllegalArgumentException("amount <= 0");
        if(command.getAmount().compareTo(remainingValue) > 0) throw new IllegalArgumentException("amount > remaining value");
        apply(new RedeemedEvent(command.getId(), command.getAmount()));
    }

    @EventSourcingHandler
    public void on(IssuedEvent event) {
        log.entry(event);
        id = event.getId();
        remainingValue = event.getAmount();
        log.trace("new state of aggregate: {}", this);
    }

    @EventSourcingHandler
    public void on(RedeemedEvent event) {
        log.entry(event);
        remainingValue = remainingValue.subtract(event.getAmount());
        log.trace("new state of aggregate: {}", this);
    }

}
