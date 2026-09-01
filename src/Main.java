import jade.core.Runtime;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import src.commons.ContainerAgentConfig;
import jade.core.Profile;
import jade.core.ProfileImpl;

public class Main {
    public static void main(String[] args) throws StaleProxyException {
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.GUI, "false");
        boolean test = false;
        if (test) {
            ContainerController mainContainer = Runtime.instance().createMainContainer(p);
            for (int i = 0; i < 5; i++) {
                mainContainer.createNewAgent("Responder_" + i,
                        "src.modelsAgents.ContractNetResponderAgent",
                        new Object[] {}).start();
            }
            mainContainer.createNewAgent("Iniciador",
                    "src.modelsAgents.ContractNetInitiatorAgent",
                    new Object[] {}).start();
        } else {
            // 1) Plataforma JADE + contenedor principal
            Runtime.instance().createMainContainer(p);

            // 2) ContainerAgentConfig crea TODOS los contenedores (uno por tipo)
            ContainerAgentConfig.initContainers();

            // 3) Se crea el Administrator en su contenedor. Una vez inicializado,
            //    el propio Administrator crea el resto de agentes leyendo los CSV.
            ContainerAgentConfig.ADMINISTRATOR_CONFIG.createAgent("Administrator", new Object[] {});
        }
    }
}




