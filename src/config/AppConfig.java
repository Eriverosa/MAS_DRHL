package src.config;

import java.util.ArrayList;
import java.util.List;

public class AppConfig {
    public List<Experiment> experiments;
    public String activeModel;
    public GraphHopper graphHopper;
    public Simulation simulation;
    public Speeds speeds;
    public StockThresholds stockThresholds;
    public Weights weights;
    public ArrayList<Integer> materialStockSizes;
    public String materialStockSizesHeaderCSV;
    public Agents agents;
    public List<Scenario> scenarios;

    public static class Experiment { public String travelModel; public boolean enabled; public int nTest; }
    public static class GraphHopper { public String baseUrl; public String profile; public boolean useStandardSpeed; }
    public static class Simulation { public int amountByPersonCc; public int delayByPersonMinutes; public int executionInitMinutes; public int executionAddMinutes; }
    public static class Speeds { public String unit; public double loadedSpeed; public double noLoadedSpeed; public double standardSpeed; }
    public static class StockThresholds { public double distributionMinStockPercent; public double donorMinStockPercent; }
    public static class Weights {
        public double requiredSupplyPersonasPercent, requiredSupplyDistancePercent, proposedAmountHelpPercent,
                proposedDistancePercent, transportationStartTimePercent, transportationEndTimePercent, transportationQuantityTransportedPercent;
    }
    public static class AgentDefinition { public String className; public String dataFile; }
    public static class Agents {
        public AgentDefinition administrator, transporter, truck, donor, distributionArea, collectionPlace;
    }
    public static class Scenario { public String name; public int iterations; public EnabledAgents enabledAgents; }
    public static class EnabledAgents { public int transporter, truck, donor, distributionArea, collectionPlace; }
}