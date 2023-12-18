package src.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SupplyActivityOrder {
    // CARGA
    private long horaInicioViajeCarga, tiempoViajeCarga, horaLlegadaViajeCarga, horaInicioCarga, tiempoCarga,
            horaFinCarga;
    // DESCARGA
    private long horaInicioViajeDescarga, tiempoViajeDescarga, horaLlegadaViajeDescarga, horaInicioDescarga,
            tiempoDescarga,
            horaFinDescarga;
    // private long horaInicioViajeDescarga, tiempoViajeDescarga,
    // horaLlegadaViajeDescarga, horaInicioDescarga,
    // tiempoDescarga, horaFinDescarga;
    // private String SupplyPoint;
    private MaterialStock materialStock;
    // private long tiempoViajeInicioActividad, horaInicioCarga,
    // tiempoViajeFinActividad, horaFinCarga;
    private String pointNameSupplyActivityProposed, pointNameSupplyActivityRequired,
            pointNameSupplyActivityTransportation;
    // private long horaSiguienteRequerimiento;

    public SupplyActivityOrder(SupplyActivity supplyActivity) {
        this.horaInicioViajeCarga = supplyActivity.getSupplyActivityTransportation().getHoraInicioViajeCarga();
        this.tiempoViajeCarga = supplyActivity.getSupplyActivityTransportation().getTiempoViajeCarga();
        this.horaLlegadaViajeCarga = supplyActivity.getSupplyActivityTransportation().getHoraInicioCarga();
        this.horaInicioCarga = supplyActivity.getSupplyActivityTransportation().getHoraInicioCarga();
        this.tiempoCarga = 200;
        this.horaFinCarga = this.horaInicioCarga + this.tiempoCarga;
        this.horaInicioViajeDescarga = this.horaFinCarga;
        this.tiempoViajeDescarga = supplyActivity.getSupplyActivityTransportation().getTiempoViajeDescarga();
        this.horaLlegadaViajeDescarga = horaInicioViajeDescarga + tiempoViajeDescarga;
        this.horaInicioDescarga = this.horaLlegadaViajeDescarga;
        this.tiempoDescarga = 100;
        this.horaFinDescarga = this.horaInicioDescarga + this.tiempoDescarga;
        this.pointNameSupplyActivityProposed = supplyActivity.getSupplyActivityProposed().getAgentName();
        this.pointNameSupplyActivityRequired = supplyActivity.getSupplyActivityRequired().getAgentName();
        this.pointNameSupplyActivityTransportation = supplyActivity.getSupplyActivityTransportation().getAgentName();
        MaterialStock materialStock = supplyActivity.getSupplyActivityProposed().getMaterialStock();
        System.out.println("supplyActivity.getSupplyActivityRequired().getCantidadPersonas()");
        System.out.println(supplyActivity.getSupplyActivityRequired().getCantidadPersonas());
        System.out.println("materialStock.toString()");
        System.out.println(materialStock.toString());
        System.out.println("supplyActivity.getSupplyActivityTransportation().getCantidadTrasladada()");
        System.out.println(supplyActivity.getSupplyActivityTransportation().getCantidadTrasladada());
        System.out.println("materialStock.getOptimeCombination(supplyActivity.getSupplyActivityTransportation().getCantidadTrasladada())");
        System.out.println(materialStock.getOptimeCombination(supplyActivity.getSupplyActivityTransportation().getCantidadTrasladada()));
        this.materialStock = materialStock
                .getOptimeCombination(supplyActivity.getSupplyActivityTransportation().getCantidadTrasladada());
        System.out.println("ln48");
    }

    public String toString(boolean prettyFormat) {
        Gson gson;
        if (prettyFormat) {
            gson = new GsonBuilder().setPrettyPrinting().create();
        } else {
            gson = new Gson();
        }
        return gson.toJson(this);
    }

    public long getHoraInicioViajeCarga() {
        return horaInicioViajeCarga;
    }

    public void setHoraInicioViajeCarga(long horaInicioViajeCarga) {
        this.horaInicioViajeCarga = horaInicioViajeCarga;
    }

    public long getTiempoViajeCarga() {
        return tiempoViajeCarga;
    }

    public void setTiempoViajeCarga(long tiempoViajeCarga) {
        this.tiempoViajeCarga = tiempoViajeCarga;
    }

    public long getHoraLlegadaViajeCarga() {
        return horaLlegadaViajeCarga;
    }

    public void setHoraLlegadaViajeCarga(long horaLlegadaViajeCarga) {
        this.horaLlegadaViajeCarga = horaLlegadaViajeCarga;
    }

    public long getHoraInicioCarga() {
        return horaInicioCarga;
    }

    public void setHoraInicioCarga(long horaInicioCarga) {
        this.horaInicioCarga = horaInicioCarga;
    }

    public long getTiempoCarga() {
        return tiempoCarga;
    }

    public void setTiempoCarga(long tiempoCarga) {
        this.tiempoCarga = tiempoCarga;
    }

    public long getHoraFinCarga() {
        return horaFinCarga;
    }

    public void setHoraFinCarga(long horaFinCarga) {
        this.horaFinCarga = horaFinCarga;
    }

    public long getHoraInicioViajeDescarga() {
        return horaInicioViajeDescarga;
    }

    public void setHoraInicioViajeDescarga(long horaInicioViajeDescarga) {
        this.horaInicioViajeDescarga = horaInicioViajeDescarga;
    }

    public long getTiempoViajeDescarga() {
        return tiempoViajeDescarga;
    }

    public void setTiempoViajeDescarga(long tiempoViajeDescarga) {
        this.tiempoViajeDescarga = tiempoViajeDescarga;
    }

    public long getHoraLlegadaViajeDescarga() {
        return horaLlegadaViajeDescarga;
    }

    public void setHoraLlegadaViajeDescarga(long horaLlegadaViajeDescarga) {
        this.horaLlegadaViajeDescarga = horaLlegadaViajeDescarga;
    }

    public long getHoraInicioDescarga() {
        return horaInicioDescarga;
    }

    public void setHoraInicioDescarga(long horaInicioDescarga) {
        this.horaInicioDescarga = horaInicioDescarga;
    }

    public long getTiempoDescarga() {
        return tiempoDescarga;
    }

    public void setTiempoDescarga(long tiempoDescarga) {
        this.tiempoDescarga = tiempoDescarga;
    }

    public long getHoraFinDescarga() {
        return horaFinDescarga;
    }

    public void setHoraFinDescarga(long horaFinDescarga) {
        this.horaFinDescarga = horaFinDescarga;
    }

    public MaterialStock getMaterialStock() {
        return materialStock;
    }

    public void setMaterialStock(MaterialStock materialStock) {
        this.materialStock = materialStock;
    }

    public String getPointNameSupplyActivityProposed() {
        return pointNameSupplyActivityProposed;
    }

    public void setPointNameSupplyActivityProposed(String pointNameSupplyActivityProposed) {
        this.pointNameSupplyActivityProposed = pointNameSupplyActivityProposed;
    }

    public String getPointNameSupplyActivityRequired() {
        return pointNameSupplyActivityRequired;
    }

    public void setPointNameSupplyActivityRequired(String pointNameSupplyActivityRequired) {
        this.pointNameSupplyActivityRequired = pointNameSupplyActivityRequired;
    }

    public String getPointNameSupplyActivityTransportation() {
        return pointNameSupplyActivityTransportation;
    }

    public void setPointNameSupplyActivityTransportation(String pointNameSupplyActivityTransportation) {
        this.pointNameSupplyActivityTransportation = pointNameSupplyActivityTransportation;
    }

}