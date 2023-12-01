package src.models;

public class Ubication implements Cloneable{
    Integer latitud, longitud;

    public Integer getLatitud() {
        return latitud;
    }

    public void setLatitud(Integer latitud) {
        this.latitud = latitud;
    }

    public Integer getLongitud() {
        return longitud;
    }

    public void setLongitud(Integer longitud) {
        this.longitud = longitud;
    }

    public Ubication(Integer latitud, Integer longitud) {
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
