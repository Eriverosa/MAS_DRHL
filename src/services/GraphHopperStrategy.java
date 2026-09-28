package src.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import src.commons.ParametersConfig;
import src.models.Ubication;

public class GraphHopperStrategy implements TravelTimeStrategy {

    private static final String BASE_URL = "http://localhost:8989/route";
    private static final String PROFILE = "car";

    private final String modelName;

    public GraphHopperStrategy(String modelName) {
        this.modelName = modelName;
    }

    @Override
    public long getTravelTime(Ubication origin, Ubication destination) {
        double distancia = getDistance(origin, destination);
        if (distancia < 0) {
            return ParametersConfig.ERROR_LONG;
        }
        double velocity = ParametersConfig.STANDARD_SPEED;
        return (long) (distancia / velocity);
    }

    private double getDistance(Ubication origin, Ubication destination) {
        JsonObject path = requestPath(origin, destination);
        if (path == null || !path.has("distance")) {
            return -1;
        }
        return path.get("distance").getAsDouble();
    }

    @Override
    public boolean routeExists(Ubication origin, Ubication destination) {
        return requestPath(origin, destination) != null;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    private JsonObject requestPath(Ubication origin, Ubication destination) {
        HttpURLConnection connection = null;
        try {
            StringBuilder url = new StringBuilder(BASE_URL);
            url.append("?point=").append(enc(origin.getLatitud() + "," + origin.getLongitud()));
            url.append("&point=").append(enc(destination.getLatitud() + "," + destination.getLongitud()));
            url.append("&profile=").append(PROFILE);
            url.append("&points_encoded=false");
            url.append("&calc_points=false");
            url.append("&instructions=false");

            connection = (HttpURLConnection) new URL(url.toString()).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != 200) {
                return null;
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(response.toString()).getAsJsonObject();
            if (!json.has("paths")) {
                return null;
            }
            JsonArray paths = json.getAsJsonArray("paths");
            return paths.size() == 0 ? null : paths.get(0).getAsJsonObject();

        } catch (Exception e) {
            System.err.println("GraphHopperStrategy error: " + e.getMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String enc(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return value;
        }
    }
}