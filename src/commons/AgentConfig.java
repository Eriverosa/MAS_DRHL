package src.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class AgentConfig {
    private static final String ADMINISTRATOR_CLASS = "Administrator";
    private static final String TRUCK_CLASS = "Truck";
    private static final String TRANSPORTER_CLASS = "Transporter";
    private static final String DISTRIBUTION_CLASS = "DistributionArea";
    private static final String DONOR_CLASS = "Donor";
    private static final String COLLECTION_CLASS = "CollectionPlace";

    public final static CreationAgentConfig ADMINISTRATOR_CONFIG = new CreationAgentConfig(
            null,
            ADMINISTRATOR_CLASS,
            null);

    public final static CreationAgentConfig TRUCK_CONFIG = new CreationAgentConfig(
            "./src/dataFiles/ArchivoAgente_Transporte.csv",
            TRUCK_CLASS,
            null);

    public final static CreationAgentConfig TRANSPORTER_CONFIG = new CreationAgentConfig(
            "./src/dataFiles/ArchivoAgente_Transporte.csv",
            TRANSPORTER_CLASS,
            null);

    public final static CreationAgentConfig DISTRIBUTION_AREA_CONFIG = new CreationAgentConfig(
            "./src/dataFiles/ArchivoAgente_PuntoDistribucion.csv",
            DISTRIBUTION_CLASS,
            null);

    public final static CreationAgentConfig DONOR_CONFIG = new CreationAgentConfig(
            "./src/dataFiles/ArchivoAgente_Donador.csv",
            DONOR_CLASS,
            null);

    public final static CreationAgentConfig COLLECTION_PLACE_CONFIG = new CreationAgentConfig(
            "./src/dataFiles/ArchivoAgente_LugarAcopio.csv",
            COLLECTION_CLASS,
            null);

    public final static ArrayList<CreationAgentConfig> CREATION_CONFIG_LIST = new ArrayList<>(
            Arrays.asList(
                    TRANSPORTER_CONFIG,
                    TRUCK_CONFIG,
                    DONOR_CONFIG,
                    DISTRIBUTION_AREA_CONFIG,
                    COLLECTION_PLACE_CONFIG));

    public static CreationAgentConfig getNextCreationAgentConfigEnable() {
        ArrayList<CreationAgentConfig> list = (ArrayList<CreationAgentConfig>) CREATION_CONFIG_LIST
                .stream()
                .filter(agentConfig -> agentConfig.getEnabledIterationBoolean() == true)
                .collect(Collectors.toList());
        return list.isEmpty() ? null : list.get(0);
    }

}
