package com.tutorial.flows;
import co.paralleluniverse.fibers.Suspendable;
import com.tutorial.contracts.CreateUserContract;
import com.tutorial.states.User;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.Arrays;

public class CreateUser {

    @InitiatingFlow
    @StartableByRPC
    public static class CreateUserInitiator extends FlowLogic<SignedTransaction>{

        private String name; //ชื่อผู้ใช้
        private Party hospitalParty; // hospital Party
        private int count;
        private double amount;

        public CreateUserInitiator(String name, Party hospitalParty,int count,double amount ) {
            this.name = name;
            this.hospitalParty = hospitalParty;
            this.amount = amount;
            this.count = count;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            /* Obtain a reference to a notary we wish to use.
             * METHOD 1: Take first notary on network, WARNING: use for test, non-prod environments, and single-notary networks only!*
             *  METHOD 2: Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)
             *  * - For production you always want to1 use Method 2 as it guarantees the expected notary is returned.
             */
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0); // METHOD 1
            //final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB")); // METHOD 2

            //Building the output AppleStamp state
            UniqueIdentifier uniqueID = new UniqueIdentifier();
            //AppleStamp newStamp = new AppleStamp(this.stampDescription,this.getOurIdentity(),this.holder,uniqueID);
            User newUser = new User(this.name,this.getOurIdentity(),this.hospitalParty,this.count,this.amount,uniqueID);

            //Compositing the transaction
            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(newUser)
                    .addCommand(new CreateUserContract.Commands.CreateUser(),
                            Arrays.asList(getOurIdentity().getOwningKey(), hospitalParty.getOwningKey()));

            // Verify that the transaction is valid.
            txBuilder.verify(getServiceHub());

            // Sign the transaction.
            final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

            // Send the state to the counterparty, and receive it back with their signature.
            FlowSession otherPartySession = initiateFlow(hospitalParty);
            final SignedTransaction fullySignedTx = subFlow(
                    new CollectSignaturesFlow(partSignedTx, Arrays.asList(otherPartySession)));

            // Notarise and record the transaction in both parties' vaults.
            return subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(otherPartySession)));
        }
    }

    @InitiatedBy(CreateUserInitiator.class)
    public static class CreateUserResponder extends FlowLogic<Void>{

        //private variable
        private FlowSession counterpartySession;

        public CreateUserResponder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Override
        @Suspendable
        public Void call() throws FlowException {
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
                @Override
                @Suspendable
                protected void checkTransaction(SignedTransaction stx) throws FlowException {
                    /*
                     * SignTransactionFlow will automatically verify the transaction and its signatures before signing it.
                     * However, just because a transaction is contractually valid doesn’t mean we necessarily want to sign.
                     * What if we don’t want to deal with the counterparty in question, or the value is too high,
                     * or we’re not happy with the transaction’s structure? checkTransaction
                     * allows us to define these additional checks. If any of these conditions are not met,
                     * we will not sign the transaction - even if the transaction and its signatures are contractually valid.
                     * ----------
                     * For this hello-world cordapp, we will not implement any additional checks.
                     * */
                }
            });

            //Stored the transaction into data base.
            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            return null;
        }
    }

}

//flow start CreateAndIssueAppleStampInitiator stampDescription: Fuji0472, holder: Peter
//run vaultQuery contractStateType: com.tutorial.states.AppleStamp