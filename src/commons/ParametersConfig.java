package src.commons;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Time;
import javax.measure.Unit;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

public class ParametersConfig {
        public final static ArrayList<Integer> MATERIAL_STOCK_SIZES = new ArrayList<>(
                        Arrays.asList(20000, 2250, 2000, 1600, 1000, 500));

        private static final Unit<Length> STANDARD_LENGTH_UNIT = Units.METRE;
        private static final Unit<Time> STANDARD_TIME_UNIT = Units.SECOND;
        private static final Unit<Speed> STANDARD_SPEED_UNIT = Units.METRE_PER_SECOND;

        // public final static String UM_SPEED = "m", UM_TIME = "s";
        public final static double REQUIRED_SUPPLY_PERSONAS_WEIGHING = 1.0, REQUIRED_SUPPLY_DISTANCIA_WEIGHING = 0.0;
        public final static double PROPOSED_START_TIME_WEIGHING = 1, PROPOSED_END_TIME_WEIGHING = 0,
                        PROPOSED_QUANTITY_TRANSPORTED_WEIGHING = 0;
        public final static Integer AMOUNT_BY_PERSON_CC = 1000;
        public final static long DELAY_BY_PERSON_TIME = pipeStandarTime(1, Units.MINUTE);
        public final static long EXECUTION_INIT_TIME = pipeStandarTime(0, Units.MINUTE);
        public final static long EXECUTION_ADD_TIME = pipeStandarTime(30, Units.MINUTE);
        public final static double LOADED_SPEED = pipeStandardSpeed(30.0, Units.KILOMETRE_PER_HOUR);
        public final static double NO_LOADED_SPEED = pipeStandardSpeed(50.0, Units.KILOMETRE_PER_HOUR);
        public final static double STANDARD_SPEED = pipeStandardSpeed(50.0, Units.KILOMETRE_PER_HOUR);
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
        //
        public final static int N_TEST = 7;

        public static long pipeStandarLength(double value, Unit<Length> inputUnit) {
                Quantity<Length> quantity = Quantities.getQuantity(value, inputUnit);
                Quantity<Length> quantityInStandardUnit = quantity.to(STANDARD_LENGTH_UNIT);
                return quantityInStandardUnit.getValue().longValue();
        }

        public static long pipeStandarTime(double value, Unit<Time> inputUnit) {
                Quantity<Time> quantity = Quantities.getQuantity(value, inputUnit);
                Quantity<Time> quantityInStandardUnit = quantity.to(STANDARD_TIME_UNIT);
                return quantityInStandardUnit.getValue().longValue();
        }

        public static long pipeStandardSpeed(double value, Unit<Speed> inputUnit) {
                Quantity<Speed> quantity = Quantities.getQuantity(value, inputUnit);
                Quantity<Speed> quantityInStandardUnit = quantity.to(STANDARD_SPEED_UNIT);
                return quantityInStandardUnit.getValue().longValue();
        }

}
