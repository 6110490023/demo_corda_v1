package com.tutorial.states;
import com.tutorial.contracts.CreateClaimContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

@BelongsToContract(CreateClaimContract.class)
public class Claim implements LinearState {

    //Private Variables
    private String name;          // name of patient  
    private double amount;        // money  
    private String typeOfService; // type of insurance
    private Party issuer;         // hospital name
    private Party receiver;       // insurance name
    // private String date;

    //LinearState required variable.
    private UniqueIdentifier linearID;

    //ALL Corda State required parameter to indicate storing parties
    private List<AbstractParty> participants;

    //Constructor Tips: Command + N in IntelliJ can auto generate constructor.
    @ConstructorForDeserialization
    public Claim (String name,double amount,String typeOfService,Party issuer, Party receiver, UniqueIdentifier linearID) {
        this.name = name;
        this.amount = amount;     
        this.typeOfService = typeOfService;
        this.issuer = issuer;
        this.receiver = receiver;
        this.linearID = linearID;
        this.participants = new ArrayList<AbstractParty>();
        this.participants.add(issuer);
        this.participants.add(receiver);
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return this.participants;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.linearID;
    }

    public Party getIssuer() {
        return issuer;
    }

    public Party getReceiver() {
        return receiver;
    }

    public String getName(){
        return name;
    }

    public double getAmount(){
        return amount;
    }

    public String getTypeOfService(){
        return typeOfService;
    }
}