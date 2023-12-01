package src.models;

import java.util.ArrayList;

public class TransportationActivityItinerary {
    private ArrayList<TransportationActivity> transportationActivities = new ArrayList<>();

    public ArrayList<TransportationActivity> getTransportationActivities() {
        return transportationActivities;
    }

    public void setTransportationActivities(ArrayList<TransportationActivity> transportationActivities) {
        this.transportationActivities = transportationActivities;
    }

    public Boolean isEmpty(){
        return this.getTransportationActivities().isEmpty();
    }

    

}
