package src.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
// import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// import src.commons.AgentConfig.CreationAgentConfig;
import src.models.SupplyActivity;

public class ScenarioConfig {
    public final List<CreationScenarioConfig> creationScenarioConfigList = new ArrayList<>();
    // public final AgentConfig agentConfig;

    public ScenarioConfig() {
        creationScenarioConfigList.add(new CreationScenarioConfig("PRE-PRINCIPAL-TERREMOTO", 100,
                Arrays.asList(
                        new BehaviourCreationScenarioConfig(AgentConfig.TRANSPORTER_CONFIG, true, 10),
                        new BehaviourCreationScenarioConfig(AgentConfig.TRUCK_CONFIG, true, 100),
                        new BehaviourCreationScenarioConfig(AgentConfig.DONOR_CONFIG, true, 10),
                        new BehaviourCreationScenarioConfig(AgentConfig.DISTRIBUTION_AREA_CONFIG, true, 0),
                        new BehaviourCreationScenarioConfig(AgentConfig.COLLECTION_PLACE_CONFIG, true, 1))));
        creationScenarioConfigList.add(new CreationScenarioConfig("POST-PRINCIPAL-TERREMOTO", 100,
                Arrays.asList(
                        new BehaviourCreationScenarioConfig(AgentConfig.TRANSPORTER_CONFIG, true, 8),
                        new BehaviourCreationScenarioConfig(AgentConfig.TRUCK_CONFIG, true, 90),
                        new BehaviourCreationScenarioConfig(AgentConfig.DONOR_CONFIG, true, 8),
                        new BehaviourCreationScenarioConfig(AgentConfig.DISTRIBUTION_AREA_CONFIG, true, 10),
                        new BehaviourCreationScenarioConfig(AgentConfig.COLLECTION_PLACE_CONFIG, true, 1))));
        creationScenarioConfigList.add(new CreationScenarioConfig("POST-REPLICAS", 100,
                Arrays.asList(
                        new BehaviourCreationScenarioConfig(AgentConfig.TRANSPORTER_CONFIG, true, 7),
                        new BehaviourCreationScenarioConfig(AgentConfig.TRUCK_CONFIG, true, 85),
                        new BehaviourCreationScenarioConfig(AgentConfig.DONOR_CONFIG, true, 7),
                        new BehaviourCreationScenarioConfig(AgentConfig.DISTRIBUTION_AREA_CONFIG, true, 15),
                        new BehaviourCreationScenarioConfig(AgentConfig.COLLECTION_PLACE_CONFIG, true, 1))));
        creationScenarioConfigList.add(new CreationScenarioConfig("POST-TSUNAMI", 100,
                Arrays.asList(
                        new BehaviourCreationScenarioConfig(AgentConfig.TRANSPORTER_CONFIG, true, 6),
                        new BehaviourCreationScenarioConfig(AgentConfig.TRUCK_CONFIG, true, 75),
                        new BehaviourCreationScenarioConfig(AgentConfig.DONOR_CONFIG, true, 6),
                        new BehaviourCreationScenarioConfig(AgentConfig.DISTRIBUTION_AREA_CONFIG, true, 18),
                        new BehaviourCreationScenarioConfig(AgentConfig.COLLECTION_PLACE_CONFIG, true, 1))));
        creationScenarioConfigList.add(new CreationScenarioConfig("POST-SEGUNDO-TERREMOTO", 100,
                Arrays.asList(
                        new BehaviourCreationScenarioConfig(AgentConfig.TRANSPORTER_CONFIG, true, 3),
                        new BehaviourCreationScenarioConfig(AgentConfig.TRUCK_CONFIG, true, 50),
                        new BehaviourCreationScenarioConfig(AgentConfig.DONOR_CONFIG, true, 3),
                        new BehaviourCreationScenarioConfig(AgentConfig.DISTRIBUTION_AREA_CONFIG, true, 23),
                        new BehaviourCreationScenarioConfig(AgentConfig.COLLECTION_PLACE_CONFIG, true, 1))));
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
        // Boolean enable;
        private List<BehaviourCreationScenarioConfig> behaviourCreationScnearionConfigList;
        private Integer nIterations, nCurrIterations;
        private String stateIteration;
        private ArrayList<SupplyActivity> supplyActivitiesList = new ArrayList<>();
        private CustomIterator iterator;
        // private CustomIterator<Integer> iterator;
        // private Boolean first = true;
        // private String state = CreationScenarioConfigStates.INITIALIZED;

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

        // public Boolean getEnable() {
        // return enable;
        // }

        // public void setEnable() {
        // this.enable = true;
        // }

        // public void setDisable() {
        // this.enable = false;
        // }

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

        // public void setEnable(Boolean enable) {
        // this.enable = enable;
        // }

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
