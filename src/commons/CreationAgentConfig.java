package src.commons;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

// Representa UN tipo de agente: su clase, su CSV de datos y su contenedor.
// Sabe crear su contenedor (initContainer) y crear agentes de su tipo (createAgent).
public class CreationAgentConfig {
    private static final String BASE_ROUTE = "src.modelsAgents.";
    String fileRoute, className, classRoute;
    Integer numInitialAgents, numInFileAgents, numCurrAgentsCreation;
    Profile profile;
    ContainerController containerController;
    Boolean enabledIterationBoolean;

    public CreationAgentConfig(String fileRoute, String className, Integer numInitialAgents) {
        this.fileRoute = fileRoute;
        this.className = className;
        this.classRoute = BASE_ROUTE + className;
        this.numInitialAgents = numInitialAgents;
        this.numInFileAgents = 0;
        this.numCurrAgentsCreation = 0;
        this.enabledIterationBoolean = true;
        // El contenedor NO se crea aqui; se crea explicitamente con initContainer().
    }

    // Crea el contenedor de este tipo una sola vez (idempotente).
    public ContainerController initContainer() {
        if (this.containerController == null) {
            this.profile = new ProfileImpl();
            this.profile.setParameter(Profile.CONTAINER_NAME, this.className + " - Container");
            this.containerController = Runtime.instance().createAgentContainer(this.profile);
        }
        return this.containerController;
    }

    // Crea y arranca un agente de este tipo dentro de su contenedor.
    public void createAgent(String agentName, Object[] args) throws StaleProxyException {
        initContainer().createNewAgent(agentName, this.classRoute, args).start();
    }

    public ContainerController getContainerController() { return initContainer(); }
    public String getFileRoute() { return fileRoute; }
    public String getClassName() { return className; }
    public String getClassRoute() { return classRoute; }
    public void setNumInFileAgents(Integer n) { this.numInFileAgents = n; }
    public Integer getNumInFileAgents() { return numInFileAgents; }
    public void setNumCurrAgentsCreation(Integer n) { this.numCurrAgentsCreation = n; }
    public Integer getNumCurrAgentsCreation() { return numCurrAgentsCreation; }
    public void setEnabledIterationBoolean(Boolean b) { this.enabledIterationBoolean = b; }
    public Boolean getEnabledIterationBoolean() { return enabledIterationBoolean; }
}
