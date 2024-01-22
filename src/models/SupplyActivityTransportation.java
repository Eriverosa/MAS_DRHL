package src.models;

import com.google.gson.Gson;

public class SupplyActivityTransportation implements Cloneable {
    long horaInicioViajeCarga, tiempoViajeCarga, horaFinViajeCarga, horaInicioCarga, tiempoCarga, horaFinCarga;
    long horaInicioViajeDescarga, tiempoViajeDescarga, horaFinViajeDescarga, horaInicioDescarga, tiempoDescarga,
            horaFinDescarga;
    String puntoCarga;
    String puntoDescarga;
    String agentName;
    Integer cantidadTrasladada;
    NegotiationTime negotiationTime;
    Integer negotiationQuantity;

    

    public NegotiationTime getNegotiationTime() {
        return negotiationTime;
    }

    public void setNegotiationTime(NegotiationTime negotiationTime) {
        this.negotiationTime = negotiationTime;
    }

    public SupplyActivityTransportation(Long tiempoInicioActividad, Long tiempoViajeCarga,
            Long tiempoViajeDescarga, String agentName, Integer cantidadTrasladada, String puntoCarga,
            String puntoDescarga) {
        this.horaInicioViajeCarga = tiempoInicioActividad;
        this.tiempoViajeCarga = tiempoViajeCarga;
        this.horaFinViajeCarga = this.horaInicioViajeCarga + this.tiempoViajeCarga;
        this.horaInicioCarga = this.horaFinViajeCarga;
        this.tiempoCarga = cantidadTrasladada * 2000;
        this.horaFinCarga = this.horaInicioCarga + this.tiempoCarga;
        this.horaInicioViajeDescarga = this.horaFinCarga;
        this.tiempoViajeDescarga = tiempoViajeDescarga;
        this.horaFinViajeDescarga = this.horaFinCarga + tiempoViajeDescarga;
        this.horaInicioDescarga = this.horaFinViajeDescarga;
        this.tiempoDescarga = cantidadTrasladada * 1000;
        this.horaFinDescarga = this.horaInicioDescarga + this.tiempoDescarga;
        this.puntoCarga = puntoCarga;
        this.puntoDescarga = puntoDescarga;
        this.agentName = agentName;
        this.cantidadTrasladada = cantidadTrasladada;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
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

    public long getHoraFinViajeCarga() {
        return horaFinViajeCarga;
    }

    public void setHoraFinViajeCarga(long horaFinViajeCarga) {
        this.horaFinViajeCarga = horaFinViajeCarga;
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

    public long getHoraFinViajeDescarga() {
        return horaFinViajeDescarga;
    }

    public void setHoraFinViajeDescarga(long horaFinViajeDescarga) {
        this.horaFinViajeDescarga = horaFinViajeDescarga;
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

    public String getPuntoCarga() {
        return puntoCarga;
    }

    public void setPuntoCarga(String puntoCarga) {
        this.puntoCarga = puntoCarga;
    }

    public String getPuntoDescarga() {
        return puntoDescarga;
    }

    public void setPuntoDescarga(String puntoDescarga) {
        this.puntoDescarga = puntoDescarga;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public Integer getCantidadTrasladada() {
        return cantidadTrasladada;
    }

    public void setCantidadTrasladada(Integer cantidadTrasladada) {
        this.cantidadTrasladada = cantidadTrasladada;
    }

    public Integer getNegotiationQuantity() {
        return negotiationQuantity;
    }

    public void setNegotiationQuantity(Integer negotiationQuantity) {
        this.negotiationQuantity = negotiationQuantity;
    }

}