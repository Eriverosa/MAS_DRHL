package src.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.google.gson.annotations.SerializedName;

public class Stock {
    @SerializedName("tamanho")
    Integer tamanho;
    @SerializedName("cantidad")
    Integer cantidad;

    public Stock(Integer tamanho, Integer cantidad) {
        this.tamanho = tamanho;
        this.cantidad = cantidad;
    }

    public Integer getTamanho() {
        return tamanho;
    }

    public void setTamanho(Integer tamanho) {
        this.tamanho = tamanho;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public void discountCantidad() {
        this.cantidad = this.cantidad - 1;
    }

    public void incrementCantidad() {
        this.cantidad = this.cantidad + 1;
    }

    public int getCantidadTotal() {
        return cantidad;
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
}
