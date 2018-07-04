package com.example.giftcard.query;

import com.example.giftcard.command.IssuedEvent;
import com.example.giftcard.command.RedeemedEvent;
import lombok.extern.slf4j.XSlf4j;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Component
@XSlf4j
public class CardSummaryProjection {

    private final EntityManager entityManager;
    private final TransactionTemplate newTransactionTemplate;
    private final QueryUpdateEmitter queryUpdateEmitter;

    public CardSummaryProjection(EntityManager entityManager, PlatformTransactionManager transactionManager,
                                 QueryUpdateEmitter queryUpdateEmitter) {
        this.entityManager = entityManager;
        /**
         * We want to commit our transactions to the read model before we emit a query update, to make sure
         * that read models responding to the update by doing a full new query, are guaranteed to see the latest state.
         * To code this concisely, we use a Spring transaction template.
         */
        this.newTransactionTemplate = new TransactionTemplate(transactionManager,
                new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        this.queryUpdateEmitter = queryUpdateEmitter;
    }

    @EventHandler
    public void on(IssuedEvent event) {
        log.trace("projecting {}", event);
        newTransactionTemplate.execute(status -> {
            entityManager.persist(new CardSummary(event.getId(), event.getAmount(), event.getAmount()));
            return null;
        });
        queryUpdateEmitter.emit(CountCardSummariesQuery.class,
                query -> event.getId().startsWith(StringUtils.defaultString(query.getFilter().getIdStartsWith())),
                new CountChangedUpdate());
    }

    @EventHandler
    public void on(RedeemedEvent event) {
        log.trace("projecting {}", event);
        CardSummary update = newTransactionTemplate.execute(status -> {
            CardSummary summary = entityManager.find(CardSummary.class, event.getId());
            summary.setRemainingValue(summary.getRemainingValue().subtract(event.getAmount()));
            return summary;
        });
        queryUpdateEmitter.emit(FetchCardSummariesQuery.class,
                query -> event.getId().startsWith(StringUtils.defaultString(query.getFilter().getIdStartsWith())),
                update);
    }

    @QueryHandler
    public List<CardSummary> handle(FetchCardSummariesQuery query) {
        log.trace("handling {}", query);
        TypedQuery<CardSummary> jpaQuery = entityManager.createNamedQuery("CardSummary.fetch", CardSummary.class);
        jpaQuery.setParameter("idStartsWith", StringUtils.defaultString(query.getFilter().getIdStartsWith()));
        jpaQuery.setFirstResult(query.getOffset());
        jpaQuery.setMaxResults(query.getLimit());
        return log.exit(jpaQuery.getResultList());
    }

    @QueryHandler
    public Integer handle(CountCardSummariesQuery query) {
        log.trace("handling {}", query);
        TypedQuery<Long> jpaQuery = entityManager.createNamedQuery("CardSummary.count", Long.class);
        jpaQuery.setParameter("idStartsWith", StringUtils.defaultString(query.getFilter().getIdStartsWith()));
        return log.exit(jpaQuery.getSingleResult().intValue());
    }

}
