package com.tutorial.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.tutorial.states.Claim;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import java.util.Arrays;

public class CreateClaim {

    @InitiatingFlow
    @StartableByRPC
    public static class CreateClaimInitiator extends FlowLogic<SignedTransaction>{

        private String name;          // name of patient  
        private double amount;        // money  
        private String typeOfService; // type of insurance
        private Party receiver; 

        public CreateClaimInitiator(String name,double amount,String typeOfService,Party receiver) {
            this.name = name;
            this.amount = amount;
            this.typeOfService = typeOfService;
            this.receiver = receiver;
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
            Claim newClaim = new Claim(this.name,this.amount,this.typeOfService,this.getOurIdentity(),this.receiver, uniqueID);
                //this.name,this.getOurIdentity(),this.hospitalParty,this.count,this.amount,uniqueID);

            //Compositing the transaction
            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(newClaim)
                    .addCommand(new ClaimContract.Commands.CreateClaim(),
                            Arrays.asList(getOurIdentity().getOwningKey(), receiver.getOwningKey()));

            // Verify that the transaction is valid.
            txBuilder.verify(getServiceHub());

            // Sign the transaction.
            final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

            // Send the state to the counterparty, and receive it back with their signature.
            FlowSession otherPartySession = initiateFlow(receiver);
            final SignedTransaction fullySignedTx = subFlow(
                    new CollectSignaturesFlow(partSignedTx, Arrays.asList(otherPartySession)));

            // Notarise and record the transaction in both parties' vaults.
            return subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(otherPartySession)));
        }
    }

    @InitiatedBy(CreateUser.CreateUserInitiator.class)
    public static class CreateUserResponder extends FlowLogic<Void>{

        //private variable
        private FlowSession counterpartySession;

        public CreateUserResponder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Override
        public Void call() throws FlowException {
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
                @Override
                @Suspendable
                protected void checkTransaction(SignedTransaction stx) throws FlowException {
                    /*
                     * SignTransactionFlow will automatically verify the transaction and its signatures before signing it.
                     * However, just because a transaction is contractually valid doesn???t mean we necessarily want to sign.
                     * What if we don???t want to deal with the counterparty in question, or the value is too high,
                     * or we???re not happy with the transaction???s structure? checkTransaction
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

