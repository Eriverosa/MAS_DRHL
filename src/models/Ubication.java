package src.models;

public class Ubication implements Cloneable{
    Double latitud, longitud;

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Ubication(Double latitud, Double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // Manejo de excepciones si es necesario
            e.printStackTrace();
            return null;
        }
    }

}
