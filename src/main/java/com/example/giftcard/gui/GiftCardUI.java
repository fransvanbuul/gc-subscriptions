package com.example.giftcard.gui;

import com.example.giftcard.command.IssueCommand;
import com.example.giftcard.command.RedeemCommand;
import com.example.giftcard.query.CardSummary;
import com.vaadin.annotations.Push;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.HeaderRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;

import java.math.BigDecimal;
import java.util.UUID;

@SpringUI
@Push
@XSlf4j
@RequiredArgsConstructor
public class GiftCardUI extends UI {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private CardSummaryDataProvider cardSummaryDataProvider;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        HorizontalLayout commandBar = new HorizontalLayout();
        commandBar.setSizeFull();
        commandBar.addComponents(issuePanel(), bulkIssuePanel(), redeemPanel());

        VerticalLayout layout = new VerticalLayout();
        layout.addComponents(commandBar, summaryGrid());
        layout.setHeight(95, Unit.PERCENTAGE);

        setContent(layout);

        UI.getCurrent().setErrorHandler(new DefaultErrorHandler() {
            @Override
            public void error(com.vaadin.server.ErrorEvent event) {
                Throwable cause = event.getThrowable();
                log.error("an error occured", cause);
                while(cause.getCause() != null) cause = cause.getCause();
                Notification.show("Error", cause.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
        });
    }

    @Override
    public void close() {
        cardSummaryDataProvider.shutDown();
        super.close();
    }

    private Panel issuePanel() {
        TextField id = new TextField("Card id");
        TextField amount = new TextField("Amount");
        Button submit = new Button("Submit");

        submit.addClickListener(evt -> {
            commandGateway.sendAndWait(new IssueCommand(id.getValue(), new BigDecimal(amount.getValue())));
            Notification.show("Success", Notification.Type.HUMANIZED_MESSAGE);
        });

        FormLayout form = new FormLayout();
        form.addComponents(id, amount, submit);
        form.setMargin(true);

        Panel panel = new Panel("Issue single card");
        panel.setContent(form);
        return panel;
    }

    private Panel bulkIssuePanel() {
        TextField number = new TextField("Number");
        TextField amount = new TextField("Amount");
        Button submit = new Button("Submit");

        submit.addClickListener(evt -> {
            for(int i = 0; i < Integer.parseInt(number.getValue()); i++) {
                String id = UUID.randomUUID().toString().substring(0, 11).toUpperCase();
                commandGateway.sendAndWait(new IssueCommand(id, new BigDecimal(amount.getValue())));
            }
            Notification.show("Success", Notification.Type.HUMANIZED_MESSAGE);
        });

        FormLayout form = new FormLayout();
        form.addComponents(number, amount, submit);
        form.setMargin(true);

        Panel panel = new Panel("Bulk issue cards");
        panel.setContent(form);
        return panel;
    }

    private Panel redeemPanel() {
        TextField id = new TextField("Card id");
        TextField amount = new TextField("Amount");
        Button submit = new Button("Submit");

        submit.addClickListener(evt -> {
            commandGateway.sendAndWait(new RedeemCommand(id.getValue(), new BigDecimal(amount.getValue())));
            Notification.show("Success", Notification.Type.HUMANIZED_MESSAGE);
        });

        FormLayout form = new FormLayout();
        form.addComponents(id, amount, submit);
        form.setMargin(true);

        Panel panel = new Panel("Redeem card");
        panel.setContent(form);
        return panel;
    }

    private Grid summaryGrid() {
        cardSummaryDataProvider = new CardSummaryDataProvider(queryGateway);
        Grid<CardSummary> grid = new Grid<>();
        Grid.Column<?, ?> idColumn = grid.addColumn(CardSummary::getId).setCaption("Card ID");
        Grid.Column<?, ?> initialValueColumn = grid.addColumn(CardSummary::getInitialValue).setCaption("Initial value");
        Grid.Column<?, ?> remainingValueColumn = grid.addColumn(CardSummary::getRemainingValue).setCaption("Remaining value");

        HeaderRow filterRow = grid.appendHeaderRow();
        TextField idStartsWith = new TextField();
        idStartsWith.setValueChangeMode(ValueChangeMode.EAGER);
        idStartsWith.setPlaceholder("Starting with");
        idStartsWith.addValueChangeListener(event -> {
            cardSummaryDataProvider.setFilter(cardSummaryDataProvider.getFilter().withIdStartsWith(event.getValue()));
            cardSummaryDataProvider.refreshAll();
        });
        filterRow.getCell(idColumn).setComponent(idStartsWith);

        grid.setSizeFull();
        grid.setDataProvider(cardSummaryDataProvider);

        return grid;
    }

}
