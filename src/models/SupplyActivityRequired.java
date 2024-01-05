package src.models;

import com.google.gson.Gson;

public class SupplyActivityRequired implements Cloneable{
    Integer cantidadPersonas;
    Integer cantidadPersonaRequired;
    Ubication ubicacion;
    String agentName;
    long horaRequerida;
    MaterialStock materialStock;
    
    public SupplyActivityRequired(Integer cantidadPersonas, Integer cantidadPersonasRequired, Ubication ubicacion, String agentName, long horaRequerida,
            MaterialStock materialStock) {
        this.cantidadPersonas = cantidadPersonas;
        this.ubicacion = ubicacion;
        this.cantidadPersonaRequired = cantidadPersonasRequired;
        this.agentName = agentName;
        this.horaRequerida = horaRequerida;
        this.materialStock = materialStock;
    }



    public Integer getCantidadPersonas() {
        return cantidadPersonas;
    }

    public void setCantidadPersonas(Integer cantidadPersonas) {
        this.cantidadPersonas = cantidadPersonas;
    }

    public Ubication getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubication ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String nombreAgente) {
        this.agentName = nombreAgente;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public Object clone() {
        try {
            // Crear una nueva instancia de RequiredSupply
            SupplyActivityRequired clonedSupply = (SupplyActivityRequired) super.clone();
            clonedSupply.ubicacion = (Ubication) this.ubicacion.clone();

            // Clonar los campos (ubicacion también debe implementar Cloneable)
            // clonedSupply.ubicacion = (Ubication) new Ubication(this.ubicacion.getLatitud(), this.ubicacion.getLongitud());
            // Devolver la instancia clonada
            return clonedSupply;
        } catch (CloneNotSupportedException e) {
            // Manejo de excepciones si es necesario
            e.printStackTrace();
            return null;
        }
    }


    public long getHoraRequerida() {
        return horaRequerida;
    }


    public void setHoraRequerida(long horaRequerida) {
        this.horaRequerida = horaRequerida;
    }



    public MaterialStock getMaterialStock() {
        return materialStock;
    }



    public void setMaterialStock(MaterialStock materialStock) {
        this.materialStock = materialStock;
    }



    public Integer getCantidadPersonaRequired() {
        return cantidadPersonaRequired;
    }



    public void setCantidadPersonaRequired(Integer cantidadPersonaRequired) {
        this.cantidadPersonaRequired = cantidadPersonaRequired;
    }

}
