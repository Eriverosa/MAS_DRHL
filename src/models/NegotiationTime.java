package src.models;

import src.commons.CustomUnits;
import src.commons.ParametersConfig;

public class NegotiationTime implements Cloneable {
    private long initializeTime = ParametersConfig.ERROR_LONG;
    private long finalizeTime = ParametersConfig.ERROR_LONG;
    private double elapsedTime = ParametersConfig.ERROR_LONG;

    public NegotiationTime() {
        this.initializeTime = System.nanoTime();
    }


    public NegotiationTime(double elapsedTime) {
        this.elapsedTime = elapsedTime;
    }



    public long getInitializeTime() {
        return initializeTime;
        // return CustomUnits.pipeStandarTime(initializeTime, CustomUnits.NANOSECOND);
    }

    public void setInitializeTime() {
        this.initializeTime = System.nanoTime();
    }

    public long getFinalizeTime() {
        return finalizeTime;
        // return CustomUnits.pipeStandarTime(finalizeTime, CustomUnits.NANOSECOND);
    }

    public void setFinalizeTime() {
        this.finalizeTime = System.nanoTime();
        this.elapsedTime = this.finalizeTime - this.initializeTime;
    }

    
    public double getElapsedTime() {
        return this.elapsedTime;
        // return (double) CustomUnits.pipeStandarTime(this.finalizeTime - this.initializeTime, CustomUnits.NANOSECOND, double.class);
    }

    @Override
    public NegotiationTime clone() {
        try {
            return (NegotiationTime) super.clone();
        } catch (CloneNotSupportedException e) {
            // Manejar la excepción de clonación si es necesario
            e.printStackTrace();
            return null; // O lanzar una excepción, según tus necesidades
        }
    }

    public void setElapsedTime(double elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
}
