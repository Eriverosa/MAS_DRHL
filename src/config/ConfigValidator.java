package src.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Valida el config.json ANTES de ejecutar la simulacion.
 * Junta todos los errores encontrados y, si hay al menos uno, detiene el
 * programa con un mensaje claro (fail-fast). Asi un typo o un valor invalido
 * nunca produce resultados contaminados.
 */
public class ConfigValidator {

    private static boolean isValidModel(String m) {
        try { src.commons.ParametersConfig.Model.valueOf(m); return true; }
        catch (Exception e) { return false; }
    }

    private static String validModelsList() {
        return Arrays.toString(src.commons.ParametersConfig.Model.values());
    }

    public static void validate() {
        AppConfig c = ConfigLoader.get();
        List<String> errors = new ArrayList<>();

        // ===== EXPERIMENTS =====
        if (c.experiments == null || c.experiments.isEmpty()) {
            errors.add("experiments: debe haber al menos un experimento.");
        } else {
            boolean algunoHabilitado = false;
            for (int i = 0; i < c.experiments.size(); i++) {
                AppConfig.Experiment e = c.experiments.get(i);
                String pos = "experiments[" + i + "]";
                if (e.travelModel == null || !isValidModel(e.travelModel)) {
                    errors.add(pos + ".travelModel: '" + (e.travelModel) + "' no es valido. Use: " + validModelsList());
                }
                if (e.nTest <= 0) {
                    errors.add(pos + ".nTest: debe ser mayor a 0 (actual: " + e.nTest + ").");
                }
                if (e.enabled) {
                    algunoHabilitado = true;
                }
            }
            if (!algunoHabilitado) {
                errors.add("experiments: al menos uno debe tener enabled=true.");
            }
        }

        // ===== SIMULATION =====
        if (c.simulation == null) {
            errors.add("simulation: falta el bloque completo.");
        } else {
            if (c.simulation.amountByPersonCc <= 0)
                errors.add("simulation.amountByPersonCc: debe ser mayor a 0.");
            if (c.simulation.delayByPersonMinutes <= 0)
                errors.add("simulation.delayByPersonMinutes: debe ser mayor a 0.");
        }

        // ===== SPEEDS =====
        if (c.speeds == null) {
            errors.add("speeds: falta el bloque completo.");
        } else {
            if (c.speeds.loadedSpeed <= 0) errors.add("speeds.loadedSpeed: debe ser mayor a 0.");
            if (c.speeds.noLoadedSpeed <= 0) errors.add("speeds.noLoadedSpeed: debe ser mayor a 0.");
            if (c.speeds.standardSpeed <= 0) errors.add("speeds.standardSpeed: debe ser mayor a 0.");
        }

        // ===== STOCK THRESHOLDS (entre 0 y 1) =====
        if (c.stockThresholds == null) {
            errors.add("stockThresholds: falta el bloque completo.");
        } else {
            if (c.stockThresholds.distributionMinStockPercent < 0 || c.stockThresholds.distributionMinStockPercent > 1)
                errors.add("stockThresholds.distributionMinStockPercent: debe estar entre 0 y 1.");
            if (c.stockThresholds.donorMinStockPercent < 0 || c.stockThresholds.donorMinStockPercent > 1)
                errors.add("stockThresholds.donorMinStockPercent: debe estar entre 0 y 1.");
        }

        // ===== HEADER DE STOCK =====
        if (c.materialStockSizesHeaderCSV == null || !c.materialStockSizesHeaderCSV.contains("{size}")) {
            errors.add("materialStockSizesHeaderCSV: debe contener el marcador '{size}' (ej: StockBotellas_{size}_cc).");
        }

        // ===== AGENTS (className + archivos CSV existen) =====
        if (c.agents == null) {
            errors.add("agents: falta el bloque completo.");
        } else {
            validarAgente(errors, "administrator", c.agents.administrator, false);
            validarAgente(errors, "transporter", c.agents.transporter, true);
            validarAgente(errors, "truck", c.agents.truck, true);
            validarAgente(errors, "donor", c.agents.donor, true);
            validarAgente(errors, "distributionArea", c.agents.distributionArea, true);
            validarAgente(errors, "collectionPlace", c.agents.collectionPlace, true);
        }

        // ===== SCENARIOS =====
        if (c.scenarios == null || c.scenarios.isEmpty()) {
            errors.add("scenarios: debe haber al menos un escenario.");
        } else {
            for (int i = 0; i < c.scenarios.size(); i++) {
                AppConfig.Scenario s = c.scenarios.get(i);
                String pos = "scenarios[" + i + "]";
                if (s.name == null || s.name.trim().isEmpty())
                    errors.add(pos + ".name: no puede estar vacio.");
                if (s.iterations <= 0)
                    errors.add(pos + ".iterations: debe ser mayor a 0.");
                if (s.enabledAgents == null)
                    errors.add(pos + ".enabledAgents: falta el bloque.");
            }
        }

        // ===== RESULTADO =====
        if (!errors.isEmpty()) {
            System.err.println("\n=========================================================");
            System.err.println("  CONFIG INVALIDO: se encontraron " + errors.size() + " error(es)");
            System.err.println("=========================================================");
            for (String err : errors) {
                System.err.println("  - " + err);
            }
            System.err.println("=========================================================");
            System.err.println("  Corrige config.json y vuelve a ejecutar.");
            System.err.println("=========================================================\n");
            System.exit(1);
        }

        System.out.println("[ConfigValidator] Config valido. Iniciando simulacion...");
    }

    private static void validarAgente(List<String> errors, String nombre, AppConfig.AgentDefinition def,
            boolean requiereArchivo) {
        if (def == null) {
            errors.add("agents." + nombre + ": falta la definicion.");
            return;
        }
        if (def.className == null || def.className.trim().isEmpty()) {
            errors.add("agents." + nombre + ".className: no puede estar vacio.");
        }
        if (requiereArchivo) {
            if (def.dataFile == null || def.dataFile.trim().isEmpty()) {
                errors.add("agents." + nombre + ".dataFile: no puede estar vacio.");
            } else if (!new File(def.dataFile).exists()) {
                errors.add("agents." + nombre + ".dataFile: el archivo no existe -> " + def.dataFile);
            }
        }
    }
}