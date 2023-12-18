package src.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.ContainerController;
import jade.core.Runtime;

public class AgentConfig {
    private static final String BASE_ROUTE = "src.modelsAgents.";
    private static final String ADMINISTRATOR_CLASS = "Administrator";
    private static final String TRUCK_CLASS = "Truck";
    private static final String TRANSPORTER_CLASS = "Transporter";
    private static final String DISTRIBUTION_CLASS = "DistributionArea";
    private static final String DONOR_CLASS = "Donor";
    private static final String COLLECTION_CLASS = "CollectionPlace";

    public class CreationAgentConfig {
        String fileRoute, className, classRoute;
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
            ;
            this.containerController = Runtime.instance().createAgentContainer(this.profile);
            this.classRoute = BASE_ROUTE + className;
            this.numInitialAgents = numInitialAgents;
            this.numInFileAgents = 0;
            this.numCurrAgentsCreation = 0;
            this.enabledIterationBoolean = true;
        }

        public String getFileRoute() {
            return fileRoute;
        }

        public void setFileRoute(String fileRoute) {
            this.fileRoute = fileRoute;
        }

        public String getClassRoute() {
            return classRoute;
        }

        public void setClassRoute(String classRoute) {
            this.classRoute = classRoute;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public Integer getNumInitialAgents() {
            return numInitialAgents;
        }

        public void setNumInitialAgents(Integer numInitialAgents) {
            this.numInitialAgents = numInitialAgents;
        }

        public Integer getNumInFileAgents() {
            return numInFileAgents;
        }

        public void setNumInFileAgents(Integer numInFileAgents) {
            this.numInFileAgents = numInFileAgents;
        }

        public Integer getNumCurrAgentsCreation() {
            return numCurrAgentsCreation;
        }

        public void setNumCurrAgentsCreation(Integer numCurrAgentsCreation) {
            this.numCurrAgentsCreation = numCurrAgentsCreation;
        }

        public Profile getProfile() {
            return profile;
        }

        public void setProfile(Profile profile) {
            this.profile = profile;
        }

        public ContainerController getContainerController() {
            return containerController;
        }

        public void setContainerController(ContainerController containerController) {
            this.containerController = containerController;
        }

        public Boolean getEnabledIterationBoolean() {
            return enabledIterationBoolean;
        }

        public void setEnabledIterationBoolean() {
            this.enabledIterationBoolean = true;
        }

        public void setDisableIterationBoolean() {
            this.enabledIterationBoolean = false;
        }

    }

    /*
     * ADMINISTRATOR - ADMINISTRADOR
     */
    public static final CreationAgentConfig ADMINISTRATOR_CONFIG = new AgentConfig().new CreationAgentConfig(
            null,
            ADMINISTRATOR_CLASS,
            null);
    /*
     * TRUCKS - CAMIONES
     */
    public static final CreationAgentConfig TRUCK_CONFIG = new AgentConfig().new CreationAgentConfig(
            "./src/dataFiles/ArchivoAgente_Transporte.csv",
            TRUCK_CLASS,
            null);
    /*
     * TRANSPORTER - TRANSPORTE
     */
    public static final CreationAgentConfig TRANSPORTER_CONFIG = new AgentConfig().new CreationAgentConfig(
            "./src/dataFiles/ArchivoAgente_Transporte.csv",
            TRANSPORTER_CLASS,
            null);
    /*
     * DISTRIBUTION AREA - PUNTO DE DISTRIBUCION
     */
    public static final CreationAgentConfig DISTRIBUTION_AREA_CONFIG = new AgentConfig().new CreationAgentConfig(
            "./src/dataFiles/ArchivoAgente_PuntoDistribucion.csv",
            DISTRIBUTION_CLASS,
            null);
    /*
     * DONOR - DONADOR
     */
    public static final CreationAgentConfig DONOR_CONFIG = new AgentConfig().new CreationAgentConfig(
            "./src/dataFiles/ArchivoAgente_Donador.csv",
            DONOR_CLASS,
            null);
    /*
     * COLLECTION PLACE - LUGAR DE ACOPIO
     */
    public static final CreationAgentConfig COLLECTION_PLACE_CONFIG = new AgentConfig().new CreationAgentConfig(
            "./src/dataFiles/ArchivoAgente_LugarAcopio.csv",
            COLLECTION_CLASS,
            null);

    public static ArrayList<CreationAgentConfig> CREATION_CONFIG_LIST = new ArrayList<>(
            Arrays.asList(
                    TRANSPORTER_CONFIG,
                    TRUCK_CONFIG,
                    DONOR_CONFIG,
                    DISTRIBUTION_AREA_CONFIG,
                    COLLECTION_PLACE_CONFIG));

    public static ArrayList<CreationAgentConfig> getCREATION_CONFIG_LIST() {
        return CREATION_CONFIG_LIST;
    }

    public static CreationAgentConfig getNextCreationAgentConfigEnable() {
        ArrayList<CreationAgentConfig> list = (ArrayList<CreationAgentConfig>) CREATION_CONFIG_LIST.stream()
                .filter(agentConfig -> agentConfig.getEnabledIterationBoolean() == true)
                .collect(Collectors.toList());
        return list.isEmpty() ? null : list.get(0);
    }

    public static void enableCreationConfigList() {
        for (CreationAgentConfig creationAgentConfig : CREATION_CONFIG_LIST) {
            creationAgentConfig.enabledIterationBoolean = true;
        }
    }

    public static void disableCreationConfigList() {
        for (CreationAgentConfig creationAgentConfig : CREATION_CONFIG_LIST) {
            creationAgentConfig.enabledIterationBoolean = false;
        }
    }
}
