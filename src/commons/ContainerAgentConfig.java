package src.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import src.config.AppConfig;

// Catalogo de tipos de agente. Declara los 6 tipos (nombres desde config.json)
// y crea TODOS los contenedores de forma explicita y ordenada (initContainers).
public class ContainerAgentConfig {
    private static final AppConfig.Agents AGENTS = ParametersConfig.getAppConfig().agents;

    public final static CreationAgentConfig ADMINISTRATOR_CONFIG      = new CreationAgentConfig(AGENTS.administrator.dataFile, AGENTS.administrator.className, null);
    public final static CreationAgentConfig TRUCK_CONFIG              = new CreationAgentConfig(AGENTS.truck.dataFile, AGENTS.truck.className, null);
    public final static CreationAgentConfig TRANSPORTER_CONFIG        = new CreationAgentConfig(AGENTS.transporter.dataFile, AGENTS.transporter.className, null);
    public final static CreationAgentConfig DISTRIBUTION_AREA_CONFIG  = new CreationAgentConfig(AGENTS.distributionArea.dataFile, AGENTS.distributionArea.className, null);
    public final static CreationAgentConfig DONOR_CONFIG              = new CreationAgentConfig(AGENTS.donor.dataFile, AGENTS.donor.className, null);
    public final static CreationAgentConfig COLLECTION_PLACE_CONFIG   = new CreationAgentConfig(AGENTS.collectionPlace.dataFile, AGENTS.collectionPlace.className, null);

    public final static ArrayList<CreationAgentConfig> CREATION_CONFIG_LIST = new ArrayList<>(
            Arrays.asList(TRANSPORTER_CONFIG, TRUCK_CONFIG, DONOR_CONFIG, DISTRIBUTION_AREA_CONFIG, COLLECTION_PLACE_CONFIG));

    // Crea el contenedor del Administrator + los 5 contenedores de tipos.
    public static void initContainers() {
        ADMINISTRATOR_CONFIG.initContainer();
        for (CreationAgentConfig config : CREATION_CONFIG_LIST) {
            config.initContainer();
        }
    }

    public static CreationAgentConfig getNextCreationAgentConfigEnable() {
        ArrayList<CreationAgentConfig> list = (ArrayList<CreationAgentConfig>) CREATION_CONFIG_LIST
                .stream().filter(c -> c.getEnabledIterationBoolean() == true).collect(Collectors.toList());
        return list.isEmpty() ? null : list.get(0);
    }
}
