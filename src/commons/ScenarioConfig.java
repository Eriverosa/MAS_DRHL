package src.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// import src.commons.AgentConfig.CreationAgentConfig;
import src.models.SupplyActivity;

public class ScenarioConfig {
    public final List<CreationScenarioConfig> creationScenarioConfigList = new ArrayList<>();
    public final AgentConfig agentConfig;

    public ScenarioConfig() {
        this.agentConfig = new AgentConfig();
        creationScenarioConfigList.add(new CreationScenarioConfig("ESCENARIO_INICIAL", 1,
                Arrays.asList(
                        new BehaviourCreationScenarioConfig(this.agentConfig.TRANSPORTER_CONFIG, true, 100),
                        new BehaviourCreationScenarioConfig(this.agentConfig.TRUCK_CONFIG, true, 100),
                        new BehaviourCreationScenarioConfig(this.agentConfig.DONOR_CONFIG, true, 1),
                        new BehaviourCreationScenarioConfig(this.agentConfig.DISTRIBUTION_AREA_CONFIG, true, 1),
                        new BehaviourCreationScenarioConfig(this.agentConfig.COLLECTION_PLACE_CONFIG, true, 100))));
        creationScenarioConfigList.add(new CreationScenarioConfig("PRIMER_SISMO", 1,
                Arrays.asList(
                        new BehaviourCreationScenarioConfig(this.agentConfig.TRANSPORTER_CONFIG, true, 100),
                        new BehaviourCreationScenarioConfig(this.agentConfig.TRUCK_CONFIG, true, 100),
                        new BehaviourCreationScenarioConfig(this.agentConfig.DONOR_CONFIG, true, 1),
                        new BehaviourCreationScenarioConfig(this.agentConfig.DISTRIBUTION_AREA_CONFIG, true, 1),
                        new BehaviourCreationScenarioConfig(this.agentConfig.COLLECTION_PLACE_CONFIG, true, 100))));
    }

    public CreationScenarioConfig getNextCreationScenarioConfigEnable(String desiredState) {
        return creationScenarioConfigList.stream()
                .filter(scenarioConfig -> scenarioConfig
                        .getStateIteration() == ParametersConfig.STATE_SCENARIO_CONFIG_NOT_INITIALIZE)
                .findFirst()
                .map(config -> {
                    if (desiredState == ParametersConfig.STATE_SCENARIO_CONFIG_NOT_INITIALIZE) {
                        config.setStateIteration(ParametersConfig.STATE_SCENARIO_CONFIG_INITIALIZE);
                    }
                    return config;
                })
                .orElse(null);
    }

    public CreationScenarioConfig getNextCreationScenarioConfigEnable() {
        return creationScenarioConfigList.stream()
                .filter(scenarioConfig -> scenarioConfig
                        .getStateIteration() == ParametersConfig.STATE_SCENARIO_CONFIG_NOT_INITIALIZE)
                .findFirst()
                .map(config -> {
                    config.setStateIteration(ParametersConfig.STATE_SCENARIO_CONFIG_INITIALIZE);
                    return config;
                })
                .orElseGet(() -> null);
    }

    public class CreationScenarioConfig {
        private String name;
        Boolean enable;
        private List<BehaviourCreationScenarioConfig> behaviourCreationScnearionConfigList;
        private Integer nIterations, nCurrIterations;
        private String stateIteration = ParametersConfig.STATE_SCENARIO_CONFIG_NOT_INITIALIZE;
        private ArrayList<SupplyActivity> supplyActivitiesList = new ArrayList<>();

        public CreationScenarioConfig(String name, Integer nIterations,
                List<BehaviourCreationScenarioConfig> behaviourCreationScnearionConfigList) {
            this.name = name;
            this.enable = true;
            this.nIterations = nIterations;
            this.behaviourCreationScnearionConfigList = behaviourCreationScnearionConfigList;
            this.nCurrIterations = 0;
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
            return this.nCurrIterations < this.nIterations;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public void setEnable(Boolean enable) {
            this.enable = enable;
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
