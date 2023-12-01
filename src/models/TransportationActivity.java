package src.models;

public class TransportationActivity {
    String inicio, fin;
    Long horaInicio, horaFin;

    public String getInicio() {
        return inicio;
    }

    public void setInicio(String inicio) {
        this.inicio = inicio;
    }

    public String getFin() {
        return fin;
    }

    public void setFin(String fin) {
        this.fin = fin;
    }

    public Long getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(Long horaInicio) {
        this.horaInicio = horaInicio;
    }

    public Long getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(Long horaFin) {
        this.horaFin = horaFin;
    }

}