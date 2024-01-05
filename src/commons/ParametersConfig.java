package src.commons;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

public class ParametersConfig {
        public final static ArrayList<Integer> MATERIAL_STOCK_SIZES = new ArrayList<>(
                        Arrays.asList(20000, 2250, 2000, 1600, 1000, 500));

        public final static double REQUIRED_SUPPLY_PERSONAS_WEIGHING = 1.0, REQUIRED_SUPPLY_DISTANCIA_WEIGHING = 0.0;
        public final static double PROPOSED_START_TIME_WEIGHING = 1, PROPOSED_END_TIME_WEIGHING = 0,
                        PROPOSED_QUANTITY_TRANSPORTED_WEIGHING = 0;
        public final static Integer AMOUNT_BY_PERSON_CC = 1000;
        // TIMES IN SECONDS
        public final static long TIME_DELAY_BY_PERSON_MS = Duration.ofSeconds(1).toMillis();
        // TIMES IN MINUTES
        public final static long TIME_EXECUTION_INIT_MS = Duration.ofMinutes(0).toMillis();
        public final static long TIME_EXECUTION_ADD_MS = Duration.ofMinutes(3).toMillis();
        // VELOCITY TRUCKS
        public final static double LOADED_SPEED = 30.0, NO_LOADED_SPEED = 50.0;
        // PERCENTAGE
        public final static double CANTIDAD_MINIMA_STOCK_PERCENT = 0.75;
        // CONSTANTS
        public final static long ERROR_LONG = -1;
        public final static String ASC_STRING = "ASCENDENTE", DESC_STRING = "DESCENDENTE";
        public final static String STATE_SCENARIO_CONFIG_NOT_INITIALIZE = "SCENARIO_CONFIG_NO_INITIALIZE",
                        STATE_SCENARIO_CONFIG_INITIALIZE = "SCENARIO_CONFIG_INITIALIZE",
                        STATE_SCENARIO_CONFIG_EXECUTING = "SCENARIO_CONFIG_EXECUTING",
                        STATE_SCENARIO_CONFIG_END = "SCENARIO_CONFIG_END";
        public final static String STATE_SUPPLY_ACTIVITY_PENDING = "STATE_SUPPLY_ACTIVITY_PENDING",
                        STATE_SUPPLY_ACTIVITY_DOING = "STATE_SUPPLY_ACTIVITY_DOING",
                        STATE_SUPPLY_ACTIVITY_DONE = "STATE_SUPPLY_ACTIVITY_DONE";
        public final static int N_TEST = 7;

}
