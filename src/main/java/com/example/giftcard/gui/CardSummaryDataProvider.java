package com.example.giftcard.gui;

import com.example.giftcard.query.*;
import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.DataChangeEvent;
import com.vaadin.data.provider.Query;
import lombok.*;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.axonframework.queryhandling.responsetypes.ResponseTypes;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@XSlf4j
@RequiredArgsConstructor
public class CardSummaryDataProvider extends AbstractBackEndDataProvider<CardSummary, Void> {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    private final QueryGateway queryGateway;

    /**
     * We need to keep track of our current subscriptions. To avoid subscriptions being modified while
     * we are processing query updates, the methods on these class are synchronized.
     */

    private SubscriptionQueryResult<List<CardSummary>, CardSummary> fetchQuery;
    private SubscriptionQueryResult<Integer, CountChangedUpdate> countQuery;

    @Getter
    @Setter
    @NonNull
    private CardSummaryFilter filter = new CardSummaryFilter(null);

    @Override
    @Synchronized
    protected Stream<CardSummary> fetchFromBackEnd(Query<CardSummary, Void> query) {
        if (fetchQuery != null) {
            fetchQuery.cancel();
            fetchQuery = null;
        }
        FetchCardSummariesQuery fetchCardSummariesQuery =
                new FetchCardSummariesQuery(query.getOffset(), query.getLimit(), filter);
        log.trace("submitting {}", fetchCardSummariesQuery);
        fetchQuery = queryGateway.subscriptionQuery(fetchCardSummariesQuery,
                ResponseTypes.multipleInstancesOf(CardSummary.class),
                ResponseTypes.instanceOf(CardSummary.class));
        fetchQuery.updates().subscribe(
                cardSummary -> {
                    log.trace("processing query update for {}: {}", fetchCardSummariesQuery, cardSummary);
                    fireEvent(new DataChangeEvent.DataRefreshEvent(this, cardSummary));
                });
        return fetchQuery.initialResult().block().stream();
    }

    @Override
    @Synchronized
    protected int sizeInBackEnd(Query<CardSummary, Void> query) {
        if (countQuery != null) {
            countQuery.cancel();
            countQuery = null;
        }
        CountCardSummariesQuery countCardSummariesQuery = new CountCardSummariesQuery(filter);
        log.trace("submitting {}", countCardSummariesQuery);
        countQuery = queryGateway.subscriptionQuery(countCardSummariesQuery,
                ResponseTypes.instanceOf(Integer.class),
                ResponseTypes.instanceOf(CountChangedUpdate.class));
        /* When the count changes (new giftcards issued), the UI has to do an entirely new query (this is
         * how the Vaadin grid works). When we're bulk issuing, it doesn't make sense to do that on every single
         * issue event. Therefore, we buffer the updates for 250 milliseconds using reactor, and do the new
         * query at most once per 250ms.
         */
        countQuery.updates().buffer(Duration.ofMillis(250)).subscribe(
                countChanged -> {
                    log.trace("processing query update for {}: {}", countCardSummariesQuery, countChanged);
                    /* This won't do, would lead to immediate new queries, looping a few times. */
//                        fireEvent(new DataChangeEvent(this));
                    executorService.execute(() -> fireEvent(new DataChangeEvent(this)));
                });
        return countQuery.initialResult().block();
    }

    @Synchronized
    public void shutDown() {
        if (fetchQuery != null) {
            fetchQuery.cancel();
            fetchQuery = null;
        }
        if (countQuery != null) {
            countQuery.cancel();
            countQuery = null;
        }
    }

}
