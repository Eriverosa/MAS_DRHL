package src.models;

import com.google.gson.Gson;

public class SupplyActivityProposed {
    MaterialStock materialStock;
    Ubication ubicacion;
    String agentName;

    public SupplyActivityProposed(MaterialStock materialStock, Ubication ubicacion, String agentName) {
        this.materialStock = materialStock;
        this.ubicacion = ubicacion;
        this.agentName = agentName;
    }

    public MaterialStock getMaterialStock() {
        return materialStock;
    }

    public void setMaterialStock(MaterialStock materialStock) {
        this.materialStock = materialStock;
    }

    public Ubication getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubication ubicacion) {
        this.ubicacion = ubicacion;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    // public ProposedSupply(String json) {
    // Gson gson = new Gson();
    // ProposedSupply objeto = gson.fromJson(json, ProposedSupply.class);
    // this.materialStock = objeto.materialStock;
    // this.ubicacion = objeto.ubicacion;
    // }

}
