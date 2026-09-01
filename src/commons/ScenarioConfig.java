package src.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import src.models.SupplyActivity;
import src.config.AppConfig;

/**
 * Configuracion de escenarios de la simulacion.
 *
 * IMPORTANTE: la logica interna (CreationScenarioConfig, BehaviourCreationScenarioConfig,
 * estados, iteradores) se mantiene IDENTICA a la version original. El unico cambio
 * es el constructor: ahora los escenarios se leen desde config.json
 * (via ParametersConfig.getAppConfig().scenarios) en lugar de estar hardcodeados.
 *
 * El orden de los agentes en cada escenario se conserva igual que antes:
 * TRANSPORTER, TRUCK, DONOR, DISTRIBUTION_AREA, COLLECTION_PLACE.
 */
public class ScenarioConfig {
    public final List<CreationScenarioConfig> creationScenarioConfigList = new ArrayList<>();

    public ScenarioConfig() {
        AppConfig appConfig = ParametersConfig.getAppConfig();

        for (AppConfig.Scenario scenario : appConfig.scenarios) {
            AppConfig.EnabledAgents enabled = scenario.enabledAgents;

            // Se respeta el mismo orden que la version original hardcodeada
            List<BehaviourCreationScenarioConfig> behaviours = Arrays.asList(
                    new BehaviourCreationScenarioConfig(ContainerAgentConfig.TRANSPORTER_CONFIG, true, enabled.transporter),
                    new BehaviourCreationScenarioConfig(ContainerAgentConfig.TRUCK_CONFIG, true, enabled.truck),
                    new BehaviourCreationScenarioConfig(ContainerAgentConfig.DONOR_CONFIG, true, enabled.donor),
                    new BehaviourCreationScenarioConfig(ContainerAgentConfig.DISTRIBUTION_AREA_CONFIG, true, enabled.distributionArea),
                    new BehaviourCreationScenarioConfig(ContainerAgentConfig.COLLECTION_PLACE_CONFIG, true, enabled.collectionPlace));

            creationScenarioConfigList.add(
                    new CreationScenarioConfig(scenario.name, scenario.iterations, behaviours));
        }
    }

    public CreationScenarioConfig getNextCreationScenarioConfigEnable(String desiredState) {
        return creationScenarioConfigList.stream()
                .filter(scenarioConfig -> scenarioConfig
                        .getStateIteration() == CreationScenarioConfig.CreationScenarioConfigStates.NOT_INITIALIZED)
                .findFirst()
                .map(config -> {
                    if (desiredState == ParametersConfig.STATE_SCENARIO_CONFIG_NOT_INITIALIZE) {
                        config.setStateIteration(ParametersConfig.STATE_SCENARIO_CONFIG_INITIALIZE);
                        config.addNCurrIteration();
                    } else if (desiredState == CreationScenarioConfig.CreationScenarioConfigStates.NOT_INITIALIZED) {
                        config.setStateIteration(CreationScenarioConfig.CreationScenarioConfigStates.INITIALIZED);
                        config.addNCurrIteration();
                    }
                    return config;
                })
                .orElse(null);
    }

    public class CreationScenarioConfig {
        private String name;
        private List<BehaviourCreationScenarioConfig> behaviourCreationScnearionConfigList;
        private Integer nIterations, nCurrIterations;
        private String stateIteration;
        private ArrayList<SupplyActivity> supplyActivitiesList = new ArrayList<>();
        private CustomIterator iterator;

        public CreationScenarioConfig(String name, Integer nIterations,
                List<BehaviourCreationScenarioConfig> behaviourCreationScnearionConfigList) {
            this.name = name;
            this.iterator = new CustomIterator(nIterations);
            this.nIterations = nIterations;
            this.behaviourCreationScnearionConfigList = behaviourCreationScnearionConfigList;
            this.nCurrIterations = 0;
            this.stateIteration = CreationScenarioConfigStates.NOT_INITIALIZED;
        }

        public String getStateIteration() {
            return stateIteration;
        }

        public void setStateIteration(String stateIteration) {
            this.stateIteration = stateIteration;
        }

        public Integer getnCurrIterations() {
            return nCurrIterations;
        }

        public void setnCurrIterations(Integer currIterations) {
            this.nCurrIterations = currIterations;
        }

        public void addNCurrIteration() {
            this.nCurrIterations = this.nCurrIterations + 1;
        }

        public boolean getIterationsPending() {
            return this.nCurrIterations <= this.nIterations;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean initial() {
            return this.getStateIteration() == CreationScenarioConfigStates.INITIALIZED;
        }

        public List<BehaviourCreationScenarioConfig> getBehaviourCreationScnearionConfigList() {
            return behaviourCreationScnearionConfigList;
        }

        public void setBehaviourCreationScnearionConfigList(
                List<BehaviourCreationScenarioConfig> behaviourCreationScnearionConfigList) {
            this.behaviourCreationScnearionConfigList = behaviourCreationScnearionConfigList;
        }

        public BehaviourCreationScenarioConfig getNextBehaviourCreationScnearionConfigListEnable() {
            List<BehaviourCreationScenarioConfig> list = this.behaviourCreationScnearionConfigList.stream()
                    .filter(scenarioConfig -> scenarioConfig.getEnable() == true)
                    .collect(Collectors.toList());
            return list.isEmpty() ? null : list.get(0);
        }

        public Integer getnIterations() {
            return nIterations;
        }

        public void setnIterations(Integer nIterations) {
            this.nIterations = nIterations;
        }

        public ArrayList<SupplyActivity> getSupplyActivitiesList() {
            return supplyActivitiesList;
        }

        public void setSupplyActivitiesList(ArrayList<SupplyActivity> supplyActivitiesList) {
            this.supplyActivitiesList = supplyActivitiesList;
        }

        public CustomIterator getIterator() {
            return iterator;
        }

        public void setIterator(CustomIterator iterator) {
            this.iterator = iterator;
        }

        public class CreationScenarioConfigStates {
            public final static String NOT_INITIALIZED = "SCENARIO_CONFIG_NOT_INITIALIZE";
            public final static String INITIALIZED = "SCENARIO_CONFIG_INITIALIZE";
            public final static String EXECUTING = "SCENARIO_CONFIG_EXECUTING";
            public final static String END = "SCENARIO_CONFIG_END";
        }
    }

    public class BehaviourCreationScenarioConfig {
        CreationAgentConfig creationAgentConfig;
        Boolean enable;
        Integer nEnabledAgents;

        public BehaviourCreationScenarioConfig(CreationAgentConfig creationAgentConfig, Boolean enable,
                Integer cantidad) {
            this.creationAgentConfig = creationAgentConfig;
            this.enable = enable;
            this.nEnabledAgents = cantidad;
        }

        public CreationAgentConfig getCreationAgentConfig() {
            return creationAgentConfig;
        }

        public Boolean getEnable() {
            return enable;
        }

        public void setEnable() {
            this.enable = true;
        }

        public void setDisable() {
            this.enable = false;
        }

        public Integer getnEnabledAgents() {
            return nEnabledAgents;
        }

        public void setnEnabledAgents(Integer nEnabledAgents) {
            this.nEnabledAgents = nEnabledAgents;
        }
    }

    public List<CreationScenarioConfig> getCreationScenarioConfigList() {
        return creationScenarioConfigList;
    }
}



