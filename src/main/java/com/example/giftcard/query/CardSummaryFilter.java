package com.example.giftcard.query;

import lombok.Value;
import lombok.experimental.Wither;

@Value
@Wither
public class CardSummaryFilter {

    String idStartsWith;

}
