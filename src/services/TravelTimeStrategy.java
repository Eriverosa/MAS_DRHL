package src.services;

import src.models.Ubication;

// Contrato comun para los modelos de calculo de tiempo/distancia de viaje.
// Permite intercambiar el modelo (Haversine vs GraphHopper) sin que los agentes
// sepan cual estan usando.
public interface TravelTimeStrategy {
    long getTravelTime(Ubication origin, Ubication destination);
    boolean routeExists(Ubication origin, Ubication destination);
    String getModelName();
}