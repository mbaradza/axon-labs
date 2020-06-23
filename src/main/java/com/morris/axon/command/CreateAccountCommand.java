package com.morris.axon.command;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

public class CreateAccountCommand {

    @TargetAggregateIdentifier
    public final String id;
    public final String accountCreator;



    public CreateAccountCommand(String id, String accountCreator) {
        this.id = id;
        this.accountCreator = accountCreator;
    }
}
