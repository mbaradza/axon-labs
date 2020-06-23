package com.morris.axon.aggregate;

import com.morris.axon.command.CloseAccountCommand;
import com.morris.axon.command.CreateAccountCommand;
import com.morris.axon.command.DepositMoneyCommand;
import com.morris.axon.command.WithdrawMoneyCommand;
import com.morris.axon.event.AccountClosedEvent;
import com.morris.axon.event.AccountCreatedEvent;
import com.morris.axon.event.MoneyDepositedEvent;
import com.morris.axon.event.MoneyWithdrawnEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateLifecycle;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.util.Assert;

import java.io.Serializable;

@Aggregate
public class BankAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    @AggregateIdentifier
    private String id;
    private String owner;
    private double balance;

    @CommandHandler
    public BankAccount(CreateAccountCommand command) {
        String id = command.id;
        String creator = command.accountCreator;

        Assert.hasLength(id, "Missing id");
        Assert.hasLength(creator, "Missing account creator");

        AggregateLifecycle.apply(new AccountCreatedEvent(id, creator, 0));
    }

    @EventSourcingHandler
    protected void on(AccountCreatedEvent event) {
        this.id = event.id;
        this.owner = event.accountCreator;
        this.balance = event.balance;
    }

    @CommandHandler
    protected void on(DepositMoneyCommand command) {
        double amount = command.amount;

        Assert.isTrue(amount > 0.0, "Deposit must be a positiv number.");

        AggregateLifecycle.apply(new MoneyDepositedEvent(id, amount));
    }

    @EventSourcingHandler
    protected void on(MoneyDepositedEvent event) {
        this.balance += event.amount;
    }


    @CommandHandler
    protected void on(WithdrawMoneyCommand command) {
        double amount = command.amount;
        String id = command.id;


        Assert.isTrue(amount > 0.0, "Withdraw must be a positiv number.");

        if(balance - amount < 0) {
            throw new InsufficientBalanceException("Insufficient balance.");
        }

        AggregateLifecycle.apply(new MoneyWithdrawnEvent(id, amount));
    }


    @CommandHandler
    protected void on(CloseAccountCommand command) {
        AggregateLifecycle.apply(new AccountClosedEvent(id));
    }

    @EventSourcingHandler
    protected void on(AccountClosedEvent event) {
        AggregateLifecycle.markDeleted();
    }



    @EventSourcingHandler
    protected void on(MoneyWithdrawnEvent event) {
        this.balance -= event.amount;
    }

    public static class InsufficientBalanceException extends RuntimeException {
        InsufficientBalanceException(String message) {
            super(message);
        }
    }


    protected BankAccount() {
        // Required by Axon to build a default Aggregate prior to Event Sourcing
    }
}
