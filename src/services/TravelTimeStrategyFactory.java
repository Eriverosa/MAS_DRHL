package src.services;

import src.commons.ParametersConfig;

// Unico lugar donde se decide el modelo. El nombre (current) viene del config.json
// via ParametersConfig.TRAVEL_MODEL, y se le pasa a la strategy.
public class TravelTimeStrategyFactory {
    private static TravelTimeStrategy cached = null;
    private static String cachedModel = null;

    public static synchronized TravelTimeStrategy getStrategy() {
        String current = ParametersConfig.TRAVEL_MODEL;
        if (cached == null || !current.equals(cachedModel)) {
            cachedModel = current;
            switch (current) {
                case "GRAPH_HOPPER":
                    cached = new GraphHopperStrategy(current);
                    break;
                case "HAVERSINE":
                default:
                    cached = new HaversineStrategy(current);
                    break;
            }
        }
        return cached;
    }
}