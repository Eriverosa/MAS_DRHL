package src.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SupplyActivity {
    private SupplyActivityRequired supplyActivityRequired;
    private SupplyActivityProposed supplyActivityProposed;
    private SupplyActivityTransportation supplyActivityTransportation;
    private SupplyActivityOrder supplyActivityOrder;

    public SupplyActivity() {
    }

    // public SupplyActivity(SupplyActivityRequired supplyActivityRequired) {
    // this.supplyActivityRequired = supplyActivityRequired;
    // this.supplyActivityProposed = null;
    // this.supplyActivityTransportation = null;
    // this.supplyActivityOrder = null;
    // }

    public SupplyActivityRequired getSupplyActivityRequired() {
        return supplyActivityRequired;
    }

    public void setSupplyActivityRequired(SupplyActivityRequired requiredSupply) {
        this.supplyActivityRequired = requiredSupply;
    }

    public SupplyActivityProposed getSupplyActivityProposed() {
        return supplyActivityProposed;
    }

    public void setSupplyActivityProposed(SupplyActivityProposed proposedSupply) {
        this.supplyActivityProposed = proposedSupply;
    }

    public SupplyActivityTransportation getSupplyActivityTransportation() {
        return supplyActivityTransportation;
    }

    public void setSupplyActivityTransportation(SupplyActivityTransportation proposedTransportation) {
        this.supplyActivityTransportation = proposedTransportation;
    }

    public SupplyActivityOrder getSupplyActivityOrder() {
        return supplyActivityOrder;
    }

    public void generateSupplyActivityOrder() {
        this.supplyActivityOrder = new SupplyActivityOrder(this);
    }

    // @Override
    public String toString(boolean prettyFormat) {
        Gson gson;
        if (prettyFormat) {
            gson = new GsonBuilder().setPrettyPrinting().create();
        } else {
            gson = new Gson();
        }
        return gson.toJson(this);
    }

    // public String toString() {
    // Gson gson = new Gson();
    // return gson.toJson(this);
    // }

}
