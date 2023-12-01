package src.models;

import com.google.gson.Gson;

public class SupplyActivityTransportation implements Cloneable {
    Long horaInicio, tiempoViajeCarga, horaInicioCarga;
    String puntoCarga;
    Long horaFin, tiempoViajeDescarga, horaInicioDescarga;
    String puntoDescarga;
    // Long tiempoDemora;
    String agentName;
    Integer cantidadTrasladada;

    public SupplyActivityTransportation(Long tiempoInicioActividad, Long tiempoViajeCarga,
            Long tiempoViajeDescarga, String agentName, Integer cantidadTrasladada) {
        this.horaInicio = tiempoInicioActividad - tiempoViajeCarga;
        this.tiempoViajeCarga = tiempoViajeCarga;
        this.horaInicioCarga = this.horaInicio + this.tiempoViajeCarga;
        this.tiempoViajeDescarga = tiempoViajeDescarga;
        this.horaFin = tiempoInicioActividad + tiempoViajeDescarga;
        this.agentName = agentName;
        this.cantidadTrasladada = cantidadTrasladada;
    }

    

    public Integer getCantidadTrasladada() {
        return cantidadTrasladada;
    }

    public void setCantidadTrasladada(Integer cantidadCarga) {
        this.cantidadTrasladada = cantidadCarga;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public Long getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(Long tiempoInicio) {
        this.horaInicio = tiempoInicio;
    }

    public Long getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(Long tiempoFin) {
        this.horaFin = tiempoFin;
    }

    public Long getTiempoViajeCarga() {
        return tiempoViajeCarga;
    }

    public void setTiempoViajeCarga(Long tiempoViajeCarga) {
        this.tiempoViajeCarga = tiempoViajeCarga;
    }

    public Long getTiempoViajeDescarga() {
        return tiempoViajeDescarga;
    }

    public void setTiempoViajeDescarga(Long tiempoViajeDescarga) {
        this.tiempoViajeDescarga = tiempoViajeDescarga;
    }



    public Long getHoraInicioCarga() {
        return horaInicioCarga;
    }



    public void setHoraInicioCarga(Long horaInicioCarga) {
        this.horaInicioCarga = horaInicioCarga;
    }



    public String getPuntoCarga() {
        return puntoCarga;
    }



    public void setPuntoCarga(String puntoCarga) {
        this.puntoCarga = puntoCarga;
    }



    public Long getHoraInicioDescarga() {
        return horaInicioDescarga;
    }



    public void setHoraInicioDescarga(Long horaInicioDescarga) {
        this.horaInicioDescarga = horaInicioDescarga;
    }



    public String getPuntoDescarga() {
        return puntoDescarga;
    }



    public void setPuntoDescarga(String puntoDescarga) {
        this.puntoDescarga = puntoDescarga;
    }

}
