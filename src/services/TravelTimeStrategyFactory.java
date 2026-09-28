package src.services;

import src.commons.ParametersConfig;

// Unico lugar donde se decide el modelo. El modelo activo viene de
// ParametersConfig.TRAVEL_MODEL (enum), resuelto desde config.json.
public class TravelTimeStrategyFactory {
    private static TravelTimeStrategy cached = null;
    private static ParametersConfig.Model cachedModel = null;

    public static synchronized TravelTimeStrategy getStrategy() {
        ParametersConfig.Model current = ParametersConfig.TRAVEL_MODEL;
        if (cached == null || current != cachedModel) {
            cachedModel = current;
            switch (current) {
                case ParametersConfig.Model.GRAPH_HOPPER:
                    cached = new GraphHopperStrategy(current.name());
                    break;
                case ParametersConfig.Model.HAVERSINE:
                default:
                    cached = new HaversineStrategy(current.name());
                    break;
            }
        }
        return cached;
    }
}