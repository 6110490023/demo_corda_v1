package com.tutorial.states;

import com.tutorial.contracts.CreateUserContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@BelongsToContract(CreateUserContract.class)
public class User implements LinearState {

    //Private Variables
    private String name; //ชื่อผู้ใช้
    private Party issuer; // insurance company
    private Party hospitalParty; // hospital Party
    private int count;
    private double amount;
    //LinearState required variable.
    private UniqueIdentifier linearID;

    //ALL Corda State required parameter to indicate storing parties
    private List<AbstractParty> participants;

    //Constructor Tips: Command + N in IntelliJ can auto generate constructor.
    @ConstructorForDeserialization
    public User(String name, Party issuer, Party hospitalParty,int count, double amount, UniqueIdentifier linearID) {
        this.name = name;
        this.issuer = issuer;
        this.hospitalParty = hospitalParty;
        this.linearID = linearID;
        this.participants = new ArrayList<AbstractParty>();
        this.participants.add(issuer);
        this.participants.add(hospitalParty);
        this.count = count;
        this.amount =amount;
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

    //Getters
    public String getName() {
        return name;
    }
    public int getCount() {
        return count;
    }
    public double getAmount() {
        return amount;
    }
    public Party getIssuer() {
        return issuer;
    }

    public Party getHospitalParty() {
        return hospitalParty;
    }

}

//Advanced tutorial add brand and type of apple for more complicated contract writing
