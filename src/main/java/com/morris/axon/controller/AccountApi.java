package com.morris.axon.controller;

import com.morris.axon.aggregate.BankAccount;
import com.morris.axon.command.CloseAccountCommand;
import com.morris.axon.command.CreateAccountCommand;
import com.morris.axon.command.DepositMoneyCommand;
import com.morris.axon.command.WithdrawMoneyCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.model.AggregateNotFoundException;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@RequestMapping("/accounts")
@RestController
public class AccountApi {

    private final CommandGateway commandGateway;
    private final EventStore eventStore;

    public AccountApi(CommandGateway commandGateway, EventStore eventStore) {
        this.commandGateway = commandGateway;
        this.eventStore = eventStore;
    }

    @PostMapping
    public CompletableFuture<String> createAccount(@RequestBody AccountOwner user) {
        String id = UUID.randomUUID().toString();
        return commandGateway.send(new CreateAccountCommand(id, user.name));
    }
    @GetMapping("{id}/events")
    public List<Object> getEvents(@PathVariable String id) {
        return eventStore.readEvents(id).asStream().map(s -> s.getPayload()).collect(Collectors.toList());
    }
    static class AccountOwner {
        public String name;
    }

    @PutMapping(path = "{accountId}/balance")
    public CompletableFuture<String> deposit(@RequestBody double amount, @PathVariable String accountId) {
        if (amount > 0) {
            System.out.println("#########=="+amount+" "+accountId);
            return commandGateway.send(new DepositMoneyCommand(accountId, amount));

        } else {
            return commandGateway.send(new WithdrawMoneyCommand(accountId, -amount));
        }
    }

    @DeleteMapping("{id}")
    public CompletableFuture<String> delete(@PathVariable String id) {
        return commandGateway.send(new CloseAccountCommand(id));
    }

    @ExceptionHandler(AggregateNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound() {
    }

    @ExceptionHandler(BankAccount.InsufficientBalanceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String insufficientBalance(BankAccount.InsufficientBalanceException exception) {
        return exception.getMessage();
    }

}