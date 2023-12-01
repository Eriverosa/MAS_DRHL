package src.services;

import jade.core.Runtime;
import jade.wrapper.StaleProxyException;
import src.commons.AgentConfig;
import jade.core.Profile;
import jade.core.ProfileImpl;

public class Main {
    public static void main(String[] args) throws StaleProxyException {
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.GUI, "false");
        Runtime.instance().createMainContainer(p);
        AgentConfig.CreationAgentConfig agentConfig = AgentConfig.ADMINISTRATOR_CONFIG;
        agentConfig.getContainerController()
                .createNewAgent("Administrator", agentConfig.getClassRoute(), new Object[] {}).start();
    }

    

}
