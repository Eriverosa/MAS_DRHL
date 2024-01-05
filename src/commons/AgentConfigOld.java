package src.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.ContainerController;
import jade.core.Runtime;

public class AgentConfigOld {
    private static final String BASE_ROUTE = "src.modelsAgents.";
    private static final String ADMINISTRATOR_CLASS = "Administrator";
    private static final String TRUCK_CLASS = "Truck";
    private static final String TRANSPORTER_CLASS = "Transporter";
    private static final String DISTRIBUTION_CLASS = "DistributionArea";
    private static final String DONOR_CLASS = "Donor";
    private static final String COLLECTION_CLASS = "CollectionPlace";

    public AgentConfigOld() {
    }

    public static class CreationAgentConfig {
        String fileRoute, className, classRoute;
        public String getClassName() {
                return className;
        }

        Integer numInitialAgents, numInFileAgents, numCurrAgentsCreation;
        Profile profile;
        ContainerController containerController;
        Boolean enabledIterationBoolean;

        public CreationAgentConfig(String fileRoute, String className, Integer numInitialAgents) {
            this.fileRoute = fileRoute;
            this.className = className;
            this.profile = new ProfileImpl() {
                {
                    setParameter(Profile.CONTAINER_NAME, className + " - Container");
                }
            };
            this.containerController = Runtime.instance().createAgentContainer(this.profile);
            this.classRoute = BASE_ROUTE + className;
            this.numInitialAgents = numInitialAgents;
            this.numInFileAgents = 0;
            this.numCurrAgentsCreation = 0;
            this.enabledIterationBoolean = true;
        }

        public Boolean getEnabledIterationBoolean() {
            return enabledIterationBoolean;
        }

        public ContainerController getContainerController() {
            return containerController;
        }

        public String getClassRoute() {
            return classRoute;
        }
    }

    /*
     * ADMINISTRATOR - ADMINISTRADOR
     */
    public final CreationAgentConfig ADMINISTRATOR_CONFIG = new CreationAgentConfig(
            null,
            ADMINISTRATOR_CLASS,
            null);

    /*
     * TRUCKS - CAMIONES
     */
    public final static CreationAgentConfig TRUCK_CONFIG = new CreationAgentConfig(
            "./src/dataFiles/ArchivoAgente_Transporte.csv",
            TRUCK_CLASS,
            null);
    /*
     * TRANSPORTER - TRANSPORTE
     */
    public final static CreationAgentConfig TRANSPORTER_CONFIG = new CreationAgentConfig(
            "./src/dataFiles/ArchivoAgente_Transporte.csv",
            TRANSPORTER_CLASS,
            null);
    /*
     * DISTRIBUTION AREA - PUNTO DE DISTRIBUCION
     */
    public final static CreationAgentConfig DISTRIBUTION_AREA_CONFIG = new CreationAgentConfig(
            "./src/dataFiles/ArchivoAgente_PuntoDistribucion.csv",
            DISTRIBUTION_CLASS,
            null);
    /*
     * DONOR - DONADOR
     */
    public final static CreationAgentConfig DONOR_CONFIG = new CreationAgentConfig(
            "./src/dataFiles/ArchivoAgente_Donador.csv",
            DONOR_CLASS,
            null);
    /*
     * COLLECTION PLACE - LUGAR DE ACOPIO
     */
    public final static CreationAgentConfig COLLECTION_PLACE_CONFIG = new CreationAgentConfig(
            "./src/dataFiles/ArchivoAgente_LugarAcopio.csv",
            COLLECTION_CLASS,
            null);

    public static final ArrayList<CreationAgentConfig> CREATION_CONFIG_LIST = new ArrayList<>(
            Arrays.asList(
                    TRANSPORTER_CONFIG,
                    TRUCK_CONFIG,
                    DONOR_CONFIG,
                    DISTRIBUTION_AREA_CONFIG,
                    COLLECTION_PLACE_CONFIG));

    public CreationAgentConfig getNextCreationAgentConfigEnable() {
        ArrayList<CreationAgentConfig> list = (ArrayList<CreationAgentConfig>) CREATION_CONFIG_LIST
                .stream()
                .filter(agentConfig -> agentConfig.getEnabledIterationBoolean() == true)
                .collect(Collectors.toList());
        return list.isEmpty() ? null : list.get(0);
    }
}
