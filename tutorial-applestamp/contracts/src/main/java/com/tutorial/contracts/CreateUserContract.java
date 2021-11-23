package com.tutorial.contracts;

import com.tutorial.states.AppleStamp;
import com.tutorial.states.User;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import static net.corda.core.contracts.ContractsDSL.requireThat; //Domain Specific Language


public class CreateUserContract implements Contract {

    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.tutorial.contracts.CrateUserContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {

        //Extract the command from the transaction.
        final CommandData commandData = tx.getCommands().get(0).getValue();

        //Verify the transaction according to the intention of the transaction
        if (commandData instanceof CreateUserContract.Commands.CreateUser){
            User output = tx.outputsOfType(User.class).get(0);
            requireThat(require -> {
                require.using("This transaction should only have one user state as output", tx.getOutputs().size() == 1);
                require.using("The output user state should have name goods", !output.getName().equals(""));
                require.using("The output user state should have amount goods", output.getAmount() > 0.0);
                require.using("The output user state should have count goods", output.getCount() > 0);
                return null;
            });
        }
        else{
            //Unrecognized Command type
            throw new IllegalArgumentException("Incorrect type of CrateUserContract Commands");
        }
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        //In our hello-world app, We will have two commands.
        class CreateUser implements CreateUserContract.Commands {}
    }
}
