package src.services;

import src.commons.CustomUnits;
import src.commons.ParametersConfig;
import src.models.Ubication;
import tech.units.indriya.unit.Units;

// MODELO BASELINE: distancia en linea recta (Haversine) / velocidad estandar.
// El nombre del modelo se recibe desde la factory (que lo saca del config.json).
public class HaversineStrategy implements TravelTimeStrategy {

    private final String modelName;

    public HaversineStrategy(String modelName) {
        this.modelName = modelName;
    }

    @Override
    public long getTravelTime(Ubication origin, Ubication destination) {
        double radioTierra = (double) CustomUnits.pipeStandarLength(6371 * 1000, Units.METRE, double.class);

        double dLat = Math.toRadians(destination.getLatitud() - origin.getLatitud());
        double dLon = Math.toRadians(destination.getLongitud() - origin.getLongitud());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(origin.getLatitud()))
                        * Math.cos(Math.toRadians(destination.getLatitud())) * Math.sin(dLon / 2)
                        * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distancia = radioTierra * c;
        double velocity = ParametersConfig.STANDARD_SPEED;
        return (long) (distancia / velocity);
    }

    @Override
    public boolean routeExists(Ubication origin, Ubication destination) {
        return true;
    }

    @Override
    public String getModelName() {
        return modelName;
    }
}