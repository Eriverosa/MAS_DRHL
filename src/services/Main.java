package src.services;

import jade.core.Runtime;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import src.commons.AgentConfig;
import src.commons.CustomUnits;
import src.commons.ParametersConfig;
import jade.core.Profile;
import jade.core.ProfileImpl;

public class Main {
    public static void main(String[] args) throws StaleProxyException {
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.GUI, "false");
        boolean test = false;
        if (test) {
            // ParametersConfig.pipeStandarTime(1, CustomUnits.MINUTE);
            // long val = System.nanoTime();
            // System.out.println(CustomUnits.pipeStandarTime(val, CustomUnits.NANOSECOND, double.class));
            // System.out.println(val);
            // System.out.println(CustomUnits.pipeStandarTime(val, CustomUnits.NANOSECOND, double.class));
            // System.exit(0);
            // Profile myProfile = new ProfileImpl();
            // myProfile.setParameter(Profile.MAIN_HOST, "localhost");
            // myProfile.setParameter(Profile.GUI, "false");

            // Obtener el contenedor principal
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
            Runtime.instance().createMainContainer(p);
            AgentConfig.ADMINISTRATOR_CONFIG.getContainerController()
                    .createNewAgent("Administrator", AgentConfig.ADMINISTRATOR_CONFIG.getClassRoute(), new Object[] {})
                    .start();
        }
    }

}
