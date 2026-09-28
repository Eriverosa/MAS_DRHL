package src.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import src.commons.ParametersConfig;
import src.config.AppConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Genera un reporte HTML autocontenido (mapa + graficos) a partir de los
 * resultados de la simulacion (CSV en src/results) y las coordenadas de los
 * agentes (CSV configurados en config.json). No requiere fetch ni servidor:
 * el JSON de datos se incrusta directamente en la plantilla al generar el
 * archivo, por eso el resultado se puede abrir con doble clic sin problemas
 * de CORS.
 *
 * Salida: ./src/results/REPORTE.html
 */
public class ReportGenerator {

    private static final String RESULTS_DIR = "./src/results/";
    private static final String TEMPLATE_PATH = "./src/resources/dashboard_template.html";
    private static final String OUTPUT_PATH = RESULTS_DIR + "REPORTE.html";
    private static final String GRAPHHOPPER_URL = "http://localhost:8989/route";

    public static void main(String[] args) {
        try {
            generar();
        } catch (Exception e) {
            System.err.println("[ReportGenerator] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void generar() throws Exception {
        AppConfig cfg = ParametersConfig.getAppConfig();

        Map<String, double[]> coords = new LinkedHashMap<>();
        Map<String, Integer> poblacion = new LinkedHashMap<>();

        cargarCoordsSimples(cfg.agents.donor.dataFile, coords);
        cargarCoordsSimples(cfg.agents.transporter.dataFile, coords);
        cargarCoordsSimples(cfg.agents.collectionPlace.dataFile, coords);
        cargarCoordsDistribucion(cfg.agents.distributionArea.dataFile, coords, poblacion);

        Map<String, Map<String, List<Map<String, Object>>>> porModelo = new LinkedHashMap<>();
        porModelo.put("gh", new LinkedHashMap<>());
        porModelo.put("hv", new LinkedHashMap<>());

        File dir = new File(RESULTS_DIR);
        File[] archivos = dir.listFiles((d, name) -> {
            String u = name.toUpperCase();
            return u.endsWith(".CSV") && u.contains("RESULTS_") && !u.contains("DISAGGREGATED");
        });

        if (archivos != null) {
            for (File f : archivos) {
                String nombre = f.getName().toUpperCase();
                String modeloKey = nombre.contains("GRAPH_HOPPER") ? "gh"
                        : (nombre.contains("HAVERSINE") ? "hv" : null);
                if (modeloKey == null) continue;
                String escenario = extraerNombreEscenario(f.getName());
                porModelo.get(modeloKey).put(escenario, parsearResultados(f));
            }
        }

        Map<String, List<double[]>> rutasGeo = new LinkedHashMap<>();
        Set<String> pares = new LinkedHashSet<>();
        for (List<Map<String, Object>> acts : porModelo.get("gh").values()) {
            for (Map<String, Object> a : acts) {
                pares.add(a.get("donador") + "|" + a.get("punto"));
            }
        }

        if (probarGraphHopper()) {
            System.out.println("[ReportGenerator] Consultando geometria de " + pares.size() + " rutas...");
            for (String par : pares) {
                String[] partes = par.split("\\|");
                double[] a = coords.get(partes[0]);
                double[] b = coords.get(partes[1]);
                if (a == null || b == null) continue;
                List<double[]> geo = consultarRuta(a, b);
                if (geo != null) rutasGeo.put(par, geo);
            }
        } else {
            System.out.println("[ReportGenerator] GraphHopper no disponible: rutas en linea recta para ambos modelos.");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("coords", coords);
        payload.put("pob", poblacion);
        payload.put("esc_gh", porModelo.get("gh"));
        payload.put("esc_hv", porModelo.get("hv"));
        payload.put("rutas_geo", rutasGeo);

        String json = new Gson().toJson(payload);
        String template = new String(Files.readAllBytes(Paths.get(TEMPLATE_PATH)), StandardCharsets.UTF_8);
        String html = template.replace("/*__DATA_JSON__*/", json);

        new File(RESULTS_DIR).mkdirs();
        Files.write(Paths.get(OUTPUT_PATH), html.getBytes(StandardCharsets.UTF_8));
        System.out.println("[ReportGenerator] Reporte generado: " + OUTPUT_PATH);
    }

    private static void cargarCoordsSimples(String path, Map<String, double[]> coords) throws Exception {
        List<String> lineas = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        for (int i = 1; i < lineas.size(); i++) {
            if (lineas.get(i).trim().isEmpty()) continue;
            String[] c = lineas.get(i).split(";");
            coords.put(c[0].trim(), new double[]{Double.parseDouble(c[1].trim()), Double.parseDouble(c[2].trim())});
        }
    }

    private static void cargarCoordsDistribucion(String path, Map<String, double[]> coords,
            Map<String, Integer> poblacion) throws Exception {
        List<String> lineas = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        for (int i = 1; i < lineas.size(); i++) {
            if (lineas.get(i).trim().isEmpty()) continue;
            String[] c = lineas.get(i).split(";");
            String nombre = c[0].trim();
            poblacion.put(nombre, Integer.parseInt(c[1].trim()));
            coords.put(nombre, new double[]{Double.parseDouble(c[2].trim()), Double.parseDouble(c[3].trim())});
        }
    }

    private static String extraerNombreEscenario(String filename) {
        String n = filename.toUpperCase();
        String[] escenarios = {"PRE-PRINCIPAL-TERREMOTO", "POST-PRINCIPAL-TERREMOTO",
                "POST-SEGUNDO-TERREMOTO", "POST-REPLICAS", "POST-TSUNAMI"};
        for (String e : escenarios) {
            if (n.startsWith(e)) return e;
        }
        return filename;
    }

    private static List<Map<String, Object>> parsearResultados(File f) throws Exception {
        List<Map<String, Object>> out = new ArrayList<>();
        List<String> lineas = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
        for (int i = 1; i < lineas.size(); i++) {
            if (lineas.get(i).trim().isEmpty()) continue;
            String[] c = lineas.get(i).split(";");
            if (c.length < 17) continue;
            try {
                Map<String, Object> a = new LinkedHashMap<>();
                a.put("tvCarga", parseNum(c[1]));
                a.put("tvDesc", parseNum(c[7]));
                a.put("finDesc", parseNum(c[11]));
                a.put("donador", c[12]);
                a.put("punto", c[13]);
                a.put("camion", c[14]);
                a.put("req", parseNum(c[15]));
                a.put("ent", parseNum(c[16]));
                a.put("transportista", c[14].split("_")[0]);
                out.add(a);
            } catch (Exception ignored) {
            }
        }
        return out;
    }

    private static long parseNum(String s) {
        return Math.round(Double.parseDouble(s.trim().replace(",", ".")));
    }

    private static boolean probarGraphHopper() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("http://localhost:8989/health").openConnection();
            con.setConnectTimeout(3000);
            con.setReadTimeout(3000);
            int code = con.getResponseCode();
            con.disconnect();
            return code == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private static List<double[]> consultarRuta(double[] a, double[] b) {
        HttpURLConnection con = null;
        try {
            String url = GRAPHHOPPER_URL
                    + "?point=" + a[0] + "," + a[1]
                    + "&point=" + b[0] + "," + b[1]
                    + "&points_encoded=false&type=json&profile=car";
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setConnectTimeout(8000);
            con.setReadTimeout(8000);
            if (con.getResponseCode() != 200) return null;

            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = r.readLine()) != null) sb.append(linea);
            }
            JsonObject json = new JsonParser().parse(sb.toString()).getAsJsonObject();
            JsonArray coordsArr = json.getAsJsonArray("paths").get(0).getAsJsonObject()
                    .getAsJsonObject("points").getAsJsonArray("coordinates");

            List<double[]> geo = new ArrayList<>();
            for (int i = 0; i < coordsArr.size(); i++) {
                JsonArray pt = coordsArr.get(i).getAsJsonArray();
                geo.add(new double[]{pt.get(1).getAsDouble(), pt.get(0).getAsDouble()});
            }
            return geo;
        } catch (Exception e) {
            return null;
        } finally {
            if (con != null) con.disconnect();
        }
    }
}
