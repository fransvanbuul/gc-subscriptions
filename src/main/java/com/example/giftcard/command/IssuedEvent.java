package com.example.giftcard.command;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class IssuedEvent {

    String id;
    BigDecimal amount;

}
