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
        CREATION_SCENARIO_CONFIG_LIST.add(new CreationScenarioConfig("ESCENARIO INICIAL",
                Arrays.asList(
                        new BehaviourCreationScenarioConfig(AgentConfig.TRANSPORTER_CONFIG, true, 10),
                        new BehaviourCreationScenarioConfig(AgentConfig.TRUCK_CONFIG, true, 100),
                        new BehaviourCreationScenarioConfig(AgentConfig.DONOR_CONFIG, true, 100),
                        new BehaviourCreationScenarioConfig(AgentConfig.DISTRIBUTION_AREA_CONFIG, true, 5),
                        new BehaviourCreationScenarioConfig(AgentConfig.COLLECTION_PLACE_CONFIG, true, 100))));
        CREATION_SCENARIO_CONFIG_LIST.add(new CreationScenarioConfig("PRIMER SISMO",
                Arrays.asList(
                        new BehaviourCreationScenarioConfig(AgentConfig.TRANSPORTER_CONFIG, true, 80),
                        // new BehaviourCreationScenarioConfig(AgentConfig.TRUCK_CONFIG, true, 100),
                        // new BehaviourCreationScenarioConfig(AgentConfig.DONOR_CONFIG, true, 100),
                        // new BehaviourCreationScenarioConfig(AgentConfig.DISTRIBUTION_AREA_CONFIG, true, 100),
                        new BehaviourCreationScenarioConfig(AgentConfig.COLLECTION_PLACE_CONFIG, true, 100))));
    }

    public static CreationScenarioConfig getNextCreationScenarioConfigEnable() {
        List<CreationScenarioConfig> list = CREATION_SCENARIO_CONFIG_LIST.stream()
                .filter(scenarioConfig -> scenarioConfig.getEnable() == true)
                .collect(Collectors.toList());
        return list.isEmpty() ? null : list.get(0);
    }

    public static class CreationScenarioConfig {
        public String name;
        Boolean enable;
        public List<BehaviourCreationScenarioConfig> behaviourCreationScnearionConfigList;

        public CreationScenarioConfig(String name,
                List<BehaviourCreationScenarioConfig> behaviourCreationScnearionConfigList) {
            this.name = name;
            this.enable = true;
            this.behaviourCreationScnearionConfigList = behaviourCreationScnearionConfigList;
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