package src.commons;

import java.util.ArrayList;

import javax.measure.Unit;
import javax.measure.quantity.Length;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Time;

import src.config.AppConfig;
import src.config.ConfigLoader;

/**
 * Punto UNICO de acceso a la configuracion del sistema.
 *
 * IMPORTANTE: la API publica (nombres de campos) se mantiene identica a la
 * version original, para NO tener que modificar los agentes ni el resto del
 * codigo. La diferencia es que ahora los valores se cargan desde el archivo
 * externo src/config/config.json (via ConfigLoader/AppConfig) en un bloque
 * estatico, en lugar de estar hardcodeados aqui.
 *
 * Las unidades se convierten a la unidad estandar del sistema reutilizando
 * CustomUnits, igual que antes (por ejemplo km/h -> m/s, minutos -> segundos).
 */
public class ParametersConfig {

    // Config cruda cargada del JSON
    private static final AppConfig CONFIG = ConfigLoader.get();

    // ===== UNIDADES ESTANDAR (constantes del sistema, no configurables) =====
    protected static final Unit<Length> STANDARD_LENGTH_UNIT = CustomUnits.METRE;
    protected static final Unit<Time> STANDARD_TIME_UNIT = CustomUnits.SECOND;
    protected static final Unit<Speed> STANDARD_SPEED_UNIT = CustomUnits.METRE_PER_SECOND;
    protected static final Unit<Time> STANDARD_RESULTS_TIME_UNIT = CustomUnits.MILLISECOND;

    // ===== TAMANOS DE STOCK =====
    public static ArrayList<Integer> MATERIAL_STOCK_SIZES = new ArrayList<>(CONFIG.materialStockSizes);
    public static final String MATERIAL_STOCK_SIZES_HEADER = CONFIG.materialStockSizesHeaderCSV;

    // ===== PESOS (percentages) =====
    public static final double REQUIRED_SUPPLY_PERSONAS_WEIGHING_PERCENT = CONFIG.weights.requiredSupplyPersonasPercent;
    public static final double REQUIRED_SUPPLY_DISTANCIA_WEIGHING_PERCENT = CONFIG.weights.requiredSupplyDistancePercent;
    public static final double PROPOSED_AMOUNT_HELP_WEIGHING_PERCENT = CONFIG.weights.proposedAmountHelpPercent;
    public static final double PROPOSED_DISTANCIA_WEIGHING_PERCENT = CONFIG.weights.proposedDistancePercent;
    public static final double TRANSPORTATION_START_TIME_WEIGHING_PERCENT = CONFIG.weights.transportationStartTimePercent;
    public static final double TRANSPORTATION_END_TIME_WEIGHING_PERCENT = CONFIG.weights.transportationEndTimePercent;
    public static final double TRANSPORTATION_QUANTITY_TRANSPORTED_WEIGHING_PERCENT = CONFIG.weights.transportationQuantityTransportedPercent;

    // ===== UMBRALES DE STOCK =====
    public static final double DISTRIBUTION_CANTIDAD_MINIMA_STOCK_PERCENT = CONFIG.stockThresholds.distributionMinStockPercent;
    public static final double DONOR_CANTIDAD_MINIMA_STOCK_PERCENT = CONFIG.stockThresholds.donorMinStockPercent;

    // ===== SIMULACION =====
    public static final Integer AMOUNT_BY_PERSON_CC = CONFIG.simulation.amountByPersonCc;

    public static final long DELAY_BY_PERSON_TIME = (long) CustomUnits.pipeStandarTime(
            CONFIG.simulation.delayByPersonMinutes, CustomUnits.MINUTE, long.class);
    public static final long EXECUTION_INIT_TIME = (long) CustomUnits.pipeStandarTime(
            CONFIG.simulation.executionInitMinutes, CustomUnits.MINUTE, long.class);
    public static final long EXECUTION_ADD_TIME = (long) CustomUnits.pipeStandarTime(
            CONFIG.simulation.executionAddMinutes, CustomUnits.MINUTE, long.class);

    // ===== VELOCIDADES (km/h -> unidad estandar) =====
    public static final double LOADED_SPEED = (double) CustomUnits.pipeStandarSpeed(
            CONFIG.speeds.loadedSpeed, CustomUnits.KILOMETRE_PER_HOUR, double.class);
    public static final double NO_LOADED_SPEED = (double) CustomUnits.pipeStandarSpeed(
            CONFIG.speeds.noLoadedSpeed, CustomUnits.KILOMETRE_PER_HOUR, double.class);
    public static final double STANDARD_SPEED = (double) CustomUnits.pipeStandarSpeed(
            CONFIG.speeds.standardSpeed, CustomUnits.KILOMETRE_PER_HOUR, double.class);

    // ===== MODELO DE VIAJE (se usara en la etapa de la heuristica) =====
    // Por ahora solo se lee como texto desde el JSON. Cuando implementemos el
    // Strategy, aqui se convertira al enum correspondiente.
    public static final String TRAVEL_MODEL = System.getProperty("travelModel", CONFIG.experiments.get(0).travelModel);

    // ===== CONSTANTES (no configurables) =====
    public static final long ERROR_LONG = -1;
    public static final String ASC_STRING = "ASCENDENTE", DESC_STRING = "DESCENDENTE";
    public static final String STATE_SCENARIO_CONFIG_NOT_INITIALIZE = "SCENARIO_CONFIG_NO_INITIALIZE",
            STATE_SCENARIO_CONFIG_INITIALIZE = "SCENARIO_CONFIG_INITIALIZE",
            STATE_SCENARIO_CONFIG_EXECUTING = "SCENARIO_CONFIG_EXECUTING",
            STATE_SCENARIO_CONFIG_END = "SCENARIO_CONFIG_END";
    public static final String STATE_SUPPLY_ACTIVITY_PENDING = "STATE_SUPPLY_ACTIVITY_PENDING",
            STATE_SUPPLY_ACTIVITY_DOING = "STATE_SUPPLY_ACTIVITY_DOING",
            STATE_SUPPLY_ACTIVITY_DONE = "STATE_SUPPLY_ACTIVITY_DONE";
    public static final String NAME_ACTIVITY_REQUIRED = "NAME_ACTIVITY_REQUIRED",
            NAME_ACTIVITY_PROPOSED = "NAME_ACTIVITY_PROPOSED",
            NAME_ACTIVITY_TRANSPORTATION = "NAME_ACTIVITY_TRANSPORTATION";

    // ===== TEST =====
    public static final int N_TEST = CONFIG.experiments.get(0).nTest;

    /** Acceso directo al AppConfig por si algun modulo necesita datos crudos (escenarios, rutas). */
    public static AppConfig getAppConfig() {
        return CONFIG;
    }
}
