package src.commons;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import src.commons.AgentConfig.CreationAgentConfig;

public class ScenarioConfig {
    public static final List<CreationScenarioConfig> CREATION_SCENARIO_CONFIG_LIST;

    static {
        CREATION_SCENARIO_CONFIG_LIST = new ArrayList<>();
        CREATION_SCENARIO_CONFIG_LIST.add(new CreationScenarioConfig("ESCENARIO_INICIAL", 100,
                Arrays.asList(
                        new BehaviourCreationScenarioConfig(AgentConfig.TRANSPORTER_CONFIG, true, 100),
                        new BehaviourCreationScenarioConfig(AgentConfig.TRUCK_CONFIG, true, 100),
                        new BehaviourCreationScenarioConfig(AgentConfig.DONOR_CONFIG, true, 1),
                        new BehaviourCreationScenarioConfig(AgentConfig.DISTRIBUTION_AREA_CONFIG, true, 1),
                        new BehaviourCreationScenarioConfig(AgentConfig.COLLECTION_PLACE_CONFIG, true, 100))));
        // CREATION_SCENARIO_CONFIG_LIST.add(new CreationScenarioConfig("PRIMER SISMO",
        // Arrays.asList(
        // new BehaviourCreationScenarioConfig(AgentConfig.TRANSPORTER_CONFIG, true,
        // 80),
        // // new BehaviourCreationScenarioConfig(AgentConfig.TRUCK_CONFIG, true, 100),
        // // new BehaviourCreationScenarioConfig(AgentConfig.DONOR_CONFIG, true, 100),
        // // new BehaviourCreationScenarioConfig(AgentConfig.DISTRIBUTION_AREA_CONFIG,
        // true, 100),
        // new BehaviourCreationScenarioConfig(AgentConfig.COLLECTION_PLACE_CONFIG,
        // true, 100))));
    }

    public static List<CreationScenarioConfig> getCREATION_SCENARIO_CONFIG_LIST(){
        return CREATION_SCENARIO_CONFIG_LIST;
    }

    public static CreationScenarioConfig getNextCreationScenarioConfigEnable(String desiredState) {
        return CREATION_SCENARIO_CONFIG_LIST.stream()
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

    public static CreationScenarioConfig getNextCreationScenarioConfigEnable() {
        return CREATION_SCENARIO_CONFIG_LIST.stream()
                .filter(scenarioConfig -> scenarioConfig
                        .getStateIteration() == ParametersConfig.STATE_SCENARIO_CONFIG_NOT_INITIALIZE)
                .findFirst()
                .map(config -> {
                    config.setStateIteration(ParametersConfig.STATE_SCENARIO_CONFIG_INITIALIZE);
                    return config;
                })
                .orElseGet(() -> null);
    }

    public static class CreationScenarioConfig {
        private String name;
        Boolean enable;
        private List<BehaviourCreationScenarioConfig> behaviourCreationScnearionConfigList;
        private Integer nIterations, nCurrIterations = 0;
        private String stateIteration = ParametersConfig.STATE_SCENARIO_CONFIG_NOT_INITIALIZE;

        public String getStateIteration() {
            return stateIteration;
        }

        public void setStateIteration(String stateIteration) {
            this.stateIteration = stateIteration;
        }

        public CreationScenarioConfig(String name, Integer nIterations,
                List<BehaviourCreationScenarioConfig> behaviourCreationScnearionConfigList) {
            this.name = name;
            this.enable = true;
            this.nIterations = nIterations;
            this.behaviourCreationScnearionConfigList = behaviourCreationScnearionConfigList;
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

    }

    public static class BehaviourCreationScenarioConfig {
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
}