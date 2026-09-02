package src.config;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import com.google.gson.Gson;

/**
 * Carga y cachea la configuracion externa (src/config/config.json) una sola vez.
 *
 * Uso: ConfigLoader.get()  ->  devuelve el AppConfig ya deserializado.
 *
 * Se inicializa de forma perezosa (lazy) y thread-safe. Si el archivo no existe
 * o esta malformado, se detiene la ejecucion con un mensaje claro, porque sin
 * configuracion la simulacion no puede continuar de forma valida.
 */
public class ConfigLoader {

    private static final String CONFIG_PATH = "./src/config/config.json";
    private static AppConfig instance = null;

    private ConfigLoader() {
        // Clase de utilidad: no instanciable
    }

    public static synchronized AppConfig get() {
        if (instance == null) {
            instance = load(CONFIG_PATH);
        }
        return instance;
    }

    /** Permite recargar en caliente si algun dia lo necesitas (tests, tuning). */
    public static synchronized AppConfig reload() {
        instance = load(CONFIG_PATH);
        return instance;
    }

    private static AppConfig load(String path) {
        try (Reader reader = new FileReader(path)) {
            AppConfig config = new Gson().fromJson(reader, AppConfig.class);
            if (config == null) {
                throw new IllegalStateException("El archivo de configuracion esta vacio: " + path);
            }
            System.out.println("[ConfigLoader] Configuracion cargada desde " + path
                    + " | Modelo de viaje: " + System.getProperty("travelModel", config.experiments.get(0).travelModel));
            return config;
        } catch (IOException e) {
            System.err.println("[ConfigLoader] No se pudo leer la configuracion en " + path);
            e.printStackTrace();
            System.exit(1);
            return null; // inalcanzable
        }
    }
}
