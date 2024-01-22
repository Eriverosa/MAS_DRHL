package src.commons;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Time;

import tech.units.indriya.quantity.Quantities;

import javax.measure.Unit;
// import tech.units.indriya.quantity.Quantities;
// import tech.units.indriya.unit.Units;

public class ParametersConfig {
        public final static ArrayList<Integer> MATERIAL_STOCK_SIZES = new ArrayList<>(
                        Arrays.asList(20000, 2250, 2000, 1600, 1000, 500));

        protected static final Unit<Length> STANDARD_LENGTH_UNIT = CustomUnits.METRE;
        protected static final Unit<Time> STANDARD_TIME_UNIT = CustomUnits.SECOND;
        protected static final Unit<Speed> STANDARD_SPEED_UNIT = CustomUnits.METRE_PER_SECOND;
        protected static final Unit<Time> STANDARD_RESULTS_TIME_UNIT = CustomUnits.MILLISECOND;
        

        // public final static String UM_SPEED = "m", UM_TIME = "s";
        public final static double REQUIRED_SUPPLY_PERSONAS_WEIGHING = 0.8, REQUIRED_SUPPLY_DISTANCIA_WEIGHING = 0.2;
        public final static double PROPOSED_START_TIME_WEIGHING = 1, PROPOSED_END_TIME_WEIGHING = 0,
                        PROPOSED_QUANTITY_TRANSPORTED_WEIGHING = 0;
        public final static Integer AMOUNT_BY_PERSON_CC = 2500;
        public final static long DELAY_BY_PERSON_TIME = (long) CustomUnits.pipeStandarTime(1, CustomUnits.MINUTE,
                        long.class);
        public final static long EXECUTION_INIT_TIME = (long) CustomUnits.pipeStandarTime(0, CustomUnits.MINUTE,
                        long.class);
        public final static long EXECUTION_ADD_TIME = (long) CustomUnits.pipeStandarTime(30, CustomUnits.MINUTE,
                        long.class);
        public final static double LOADED_SPEED = (double) CustomUnits.pipeStandarSpeed(30.0,
                        CustomUnits.KILOMETRE_PER_HOUR, double.class);
        public final static double NO_LOADED_SPEED = (double) CustomUnits.pipeStandarSpeed(50.0,
                        CustomUnits.KILOMETRE_PER_HOUR, double.class);
        public final static double STANDARD_SPEED = (double) CustomUnits.pipeStandarSpeed(50.0,
                        CustomUnits.KILOMETRE_PER_HOUR, double.class);

        // PERCENTAGE
        public final static double DISTRIBUTION_CANTIDAD_MINIMA_STOCK_PERCENT = 0.75;
        public final static double DONOR_CANTIDAD_MINIMA_STOCK_PERCENT = 0.20;

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
        //
        public final static int N_TEST = 7;

}
