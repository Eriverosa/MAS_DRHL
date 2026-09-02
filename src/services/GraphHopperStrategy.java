package src.services;

import src.models.Ubication;

// MODELO POR CALLES: se implementara en el siguiente paso (GraphHopper).
// El nombre del modelo se recibe desde la factory (que lo saca del config.json),
// no se escribe a mano.
public class GraphHopperStrategy implements TravelTimeStrategy {

    private final String modelName;

    public GraphHopperStrategy(String modelName) {
        this.modelName = modelName;
    }

    @Override
    public long getTravelTime(Ubication origin, Ubication destination) {
        throw new UnsupportedOperationException("GraphHopperStrategy aun no implementado");
    }

    @Override
    public boolean routeExists(Ubication origin, Ubication destination) {
        throw new UnsupportedOperationException("GraphHopperStrategy aun no implementado");
    }

    @Override
    public String getModelName() {
        return modelName;
    }
}