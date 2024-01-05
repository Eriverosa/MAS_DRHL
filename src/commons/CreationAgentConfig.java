package src.commons;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;

public class CreationAgentConfig {
    private static final String BASE_ROUTE = "src.modelsAgents.";
    String fileRoute, className, classRoute;

    public String getFileRoute() {
        return fileRoute;
    }

    public String getClassName() {
        return className;
    }

    Integer numInitialAgents, numInFileAgents, numCurrAgentsCreation;

    public void setNumInFileAgents(Integer numInFileAgents) {
        this.numInFileAgents = numInFileAgents;
    }

    public Integer getNumInFileAgents() {
        return numInFileAgents;
    }

    public void setNumCurrAgentsCreation(Integer numCurrAgentsCreation) {
        this.numCurrAgentsCreation = numCurrAgentsCreation;
    }

    Profile profile;
    ContainerController containerController;
    Boolean enabledIterationBoolean;

    public void setEnabledIterationBoolean(Boolean enabledIterationBoolean) {
        this.enabledIterationBoolean = enabledIterationBoolean;
    }

    public CreationAgentConfig(String fileRoute, String className, Integer numInitialAgents) {
        this.fileRoute = fileRoute;
        this.className = className;
        Runtime runtime = Runtime.instance();
        this.profile = new ProfileImpl() {
            {
                setParameter(Profile.CONTAINER_NAME, className + " - Container");
            }
        };
        this.containerController = runtime.createAgentContainer(this.profile);
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

    public Integer getNumCurrAgentsCreation() {
        return numCurrAgentsCreation;
    }

    public String getClassRoute() {
        return classRoute;
    }

}
