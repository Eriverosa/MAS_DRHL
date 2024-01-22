package src.commons;

import java.util.Arrays;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Time;

import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

public class CustomUnits extends Units {
    public static final Unit<Time> MILLISECOND = Units.SECOND.multiply(0.001).asType(Time.class);
    public static final Unit<Time> NANOSECOND = Units.SECOND.multiply(1e-9).asType(Time.class);

    public static Object pipeStandarLength(Number value, Unit<Length> inputUnit, Class<?> returnType) {
        Quantity<Length> quantity = Quantities.getQuantity(value, inputUnit);
        Quantity<Length> quantityInStandardUnit = quantity.to(ParametersConfig.STANDARD_LENGTH_UNIT);
        return convertQuantity(quantityInStandardUnit, returnType);
    }

    public static Object pipeStandarTime(Number value, Unit<Time> inputUnit, Class<?> returnType) {
        Quantity<Time> quantity = Quantities.getQuantity(value, inputUnit);
        Quantity<Time> quantityInStandardUnit = quantity.to(ParametersConfig.STANDARD_TIME_UNIT);
        return convertQuantity(quantityInStandardUnit, returnType);
    }

    public static Object pipeStandarTime(Number value, Unit<Time> inputUnit, Unit<Time> convertUnit, Class<?> returnType) {
        Quantity<Time> quantity = Quantities.getQuantity(value, inputUnit);
        Quantity<Time> quantityInStandardUnit = quantity.to(convertUnit);
        return convertQuantity(quantityInStandardUnit, returnType);
    }

    public static Object pipeStandarSpeed(Number value, Unit<Speed> inputUnit, Class<?> returnType) {
        Quantity<Speed> quantity = Quantities.getQuantity(value, inputUnit);
        Quantity<Speed> quantityInStandardUnit = quantity.to(ParametersConfig.STANDARD_SPEED_UNIT);
        return convertQuantity(quantityInStandardUnit, returnType);
    }

    public static Object convertQuantity(Quantity<?> quantity, Class<?> returnType) {
        if (Arrays.asList(Long.class, long.class).contains(returnType)) {
            return quantity.getValue().longValue();
        } else if (Arrays.asList(Double.class, double.class).contains(returnType)) {
            return quantity.getValue().doubleValue();
        } else if (Arrays.asList(Integer.class, int.class).contains(returnType)) {
            return quantity.getValue().intValue();
        } else {
            throw new IllegalArgumentException("Tipo de retorno no válido");
        }
    }
}
