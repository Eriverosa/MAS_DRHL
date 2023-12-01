package src.models;

import com.google.gson.Gson;

public class SupplyActivityOrder {
    private long horaInicioActividad, horaFinActividad;
    private String SupplyPoint;
    private MaterialStock materialStock;
    private long tiempoViajeInicioActividad, horaInicioCarga, tiempoViajeFinActividad, horaFinCarga;
    private String pointNameSupplyActivityProposed, pointNameSupplyActivityRequired,
            pointNameSupplyActivityTransportation;
    private long horaSiguienteRequerimiento;

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public SupplyActivityOrder(SupplyActivity supplyActivityProposal) {
        this.horaInicioActividad = supplyActivityProposal.getSupplyActivityTransportation().getHoraInicio();
        this.tiempoViajeInicioActividad = supplyActivityProposal.getSupplyActivityTransportation()
                .getTiempoViajeCarga();
        this.horaInicioCarga = supplyActivityProposal.getSupplyActivityTransportation().getHoraInicioCarga();
        this.horaFinActividad = supplyActivityProposal.getSupplyActivityTransportation().getHoraFin();
        this.tiempoViajeFinActividad = supplyActivityProposal.getSupplyActivityTransportation()
                .getTiempoViajeDescarga();
        this.horaFinCarga = supplyActivityProposal.getSupplyActivityTransportation().getHoraFin();
        this.materialStock = supplyActivityProposal.getSupplyActivityProposed().getMaterialStock();
        this.pointNameSupplyActivityProposed = supplyActivityProposal.getSupplyActivityProposed().getAgentName();
        this.pointNameSupplyActivityRequired = supplyActivityProposal.getSupplyActivityRequired().getAgentName();
        this.pointNameSupplyActivityTransportation = supplyActivityProposal.getSupplyActivityTransportation()
                .getAgentName();
    }
    
    public long getHoraInicioActividad() {
        return horaInicioActividad;
    }

    public void setHoraInicioActividad(long horaInicioActividad) {
        this.horaInicioActividad = horaInicioActividad;
    }

    public long getHoraFinActividad() {
        return horaFinActividad;
    }

    public void setHoraFinActividad(long horaFinActividad) {
        this.horaFinActividad = horaFinActividad;
    }

    public String getSupplyPoint() {
        return SupplyPoint;
    }

    public void setSupplyPoint(String supplyPoint) {
        SupplyPoint = supplyPoint;
    }

    public MaterialStock getMaterialStock() {
        return materialStock;
    }

    public void setMaterialStock(MaterialStock materialStock) {
        this.materialStock = materialStock;
    }

    public long getTiempoViajeInicioActividad() {
        return tiempoViajeInicioActividad;
    }

    public void setTiempoViajeInicioActividad(long tiempoViajeInicioActividad) {
        this.tiempoViajeInicioActividad = tiempoViajeInicioActividad;
    }

    public long getHoraInicioCarga() {
        return horaInicioCarga;
    }

    public void setHoraInicioCarga(long horaInicioCarga) {
        this.horaInicioCarga = horaInicioCarga;
    }

    public long getTiempoViajeFinActividad() {
        return tiempoViajeFinActividad;
    }

    public void setTiempoViajeFinActividad(long tiempoViajeFinActividad) {
        this.tiempoViajeFinActividad = tiempoViajeFinActividad;
    }

    public long getHoraFinCarga() {
        return horaFinCarga;
    }

    public void setHoraFinCarga(long horaFinCarga) {
        this.horaFinCarga = horaFinCarga;
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

    public long getHoraSiguienteRequerimiento() {
        return horaSiguienteRequerimiento;
    }

    public void setHoraSiguienteRequerimiento(long horaSiguienteRequerimiento) {
        this.horaSiguienteRequerimiento = horaSiguienteRequerimiento;
    }
    
    public void estimarHoraSiguienteRequerimiento(MaterialStock materialStock){
        
    }

}