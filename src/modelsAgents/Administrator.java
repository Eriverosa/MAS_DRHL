package src.modelsAgents;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetInitiator;
import jade.proto.ContractNetResponder;
import jade.proto.SimpleAchieveREInitiator;
import jade.proto.SubscriptionInitiator;
import jade.proto.SubscriptionResponder;
import jade.wrapper.StaleProxyException;
import src.behaviours.SimpleResponder;
// import src.commons.AgentConfig;
import src.commons.AgentConfig;
import src.commons.FileGenerator;
import src.commons.ParametersConfig;
import src.commons.ScenarioConfig;
import src.models.SupplyActivity;
import src.models.SupplyActivityProposed;
import src.models.SupplyActivityRequired;
import src.models.SupplyActivityTransportation;
import src.commons.ScenarioConfig.BehaviourCreationScenarioConfig;
import src.commons.ScenarioConfig.CreationScenarioConfig;
import src.commons.CreationAgentConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

public class Administrator extends Agent {
    private final DFHelper DF_HELPER = DFHelper.getInstance();
    private long currExecutionTime;
    private boolean first = true;
    private ScenarioConfig scenarioConfig;
    private AgentConfig agentConfig;
    private CreationScenarioConfig currCreationScenarioConfig;

    @Override
    protected void setup() {
        this.scenarioConfig = new ScenarioConfig();
        this.agentConfig = new AgentConfig();
        DF_HELPER.registrarServicio(this);
        this.BR_CreacionFinalizada();
        this.BR_EndSimulation();
        this.loadDataGenerateAgent();
    }

    public void loadDataGenerateAgent() {

        // CreationAgentConfig currAgentConfig = this.agentConfig.
        CreationAgentConfig currAgentConfig = this.agentConfig.getNextCreationAgentConfigEnable();
        if (!Objects.isNull(currAgentConfig)) {
            if (Objects.equals(currAgentConfig.getClassName(), this.agentConfig.TRANSPORTER_CONFIG.getClassName())) {
                this.crearAgentesTransporte();
            } else if (Objects.equals(currAgentConfig.getClassName(), this.agentConfig.TRUCK_CONFIG.getClassName())) {
                this.crearAgentesCamiones();
            } else if (Objects.equals(currAgentConfig.getClassName(), this.agentConfig.DONOR_CONFIG.getClassName())) {
                this.crearAgentesDonador();
            } else if (Objects.equals(currAgentConfig.getClassName(),
                    this.agentConfig.DISTRIBUTION_AREA_CONFIG.getClassName())) {
                this.crearAgentesPuntoDistribucion();
            } else if (Objects.equals(currAgentConfig.getClassName(),
                    this.agentConfig.COLLECTION_PLACE_CONFIG.getClassName())) {
                this.crearAgentesLugarAcopio();
            } else {
                System.out.println("Error");
                System.exit(0);
            }
        } else {
            // AgentConfig.enableCreationConfigList();
            this.currCreationScenarioConfig = this.scenarioConfig
                    .getNextCreationScenarioConfigEnable(ParametersConfig.STATE_SCENARIO_CONFIG_NOT_INITIALIZE);
            this.F_UpdateState();
        }
    }

    public void F_UpdateState() {
        DF_HELPER.println(this, "PROCESS CHANGE STATE OF AGENTS");
        List<Agent> listAgentsEnable = new ArrayList<>();
        List<Agent> listAgentsDisable = new ArrayList<>();
        for (BehaviourCreationScenarioConfig behaviorConfig : this.currCreationScenarioConfig
                .getBehaviourCreationScnearionConfigList()) {

            ArrayList<Agent> agentsList = DF_HELPER.getAgentsList(behaviorConfig.getCreationAgentConfig());
            Collections.shuffle(agentsList);
            Integer nEnabledAgents = Objects.isNull(behaviorConfig.getnEnabledAgents()) ? agentsList.size()
                    : Math.min(behaviorConfig.getnEnabledAgents(), agentsList.size());
            listAgentsEnable.addAll(agentsList.subList(0, nEnabledAgents));
            listAgentsDisable.addAll(agentsList.subList(nEnabledAgents, agentsList.size()));
        }
        this.BI_UpdateState(listAgentsEnable, listAgentsDisable, true);
    }

    public void BI_UpdateState(List<Agent> listAgentsEnable, List<Agent> listAgentsDisable,
            boolean enabBoolean) {
        List<Agent> listAgents = enabBoolean ? listAgentsEnable : listAgentsDisable;
        ACLMessage msg = DF_HELPER.ACL_MESSAGE_UPDATE_AGENT_STATE;
        msg.setContent(Boolean.toString(enabBoolean));
        for (Agent agent : listAgents) {
            msg.addReceiver(agent.getAID());
        }

        addBehaviour(new AchieveREInitiator(this, msg) {
            protected void handleAllResponses(Vector responses) {
                DF_HELPER.println(myAgent,
                        "Lista de agentes " + (enabBoolean ? "habilitados" : "deshabilitados") + " su estado");
            }

            public int onEnd() {
                super.onEnd();
                if (enabBoolean) {
                    BI_UpdateState(listAgentsEnable, listAgentsDisable, false);
                } else {
                    DF_HELPER.println(myAgent, "Proceso de actualización de agentes finalizada");
                    BI_UpdateTimeEvent();
                }
                return 0;
            }
        });
    }

    public void BI_UpdateTimeEvent() {
        DF_HELPER.println("UPDATE TIME EVENT");
        // System.out.println(currCreationScenarioConfig);
        this.currExecutionTime = (this.currCreationScenarioConfig.getnCurrIterations() == 0)
                ? ParametersConfig.TIME_EXECUTION_INIT_MS
                : (this.currExecutionTime + ParametersConfig.TIME_EXECUTION_ADD_MS);
        ACLMessage msg = DF_HELPER.ACL_MESSAGE_UPDATE_TIME_EVENT;
        msg.setContent(String.valueOf(currExecutionTime));
        ArrayList<Agent> agentsList = new ArrayList<>();
        agentsList.addAll(DF_HELPER.getListRegisteredDistributionArea());
        for (Agent agent : agentsList) {
            msg.addReceiver(agent.getAID());
        }
        this.addBehaviour(new AchieveREInitiator(this, msg) {
            @Override
            protected void handleAllResponses(Vector responses) {
                System.out.println("Llegaron todos los mensajes.");
            }

            @Override
            public int onEnd() {
                super.onEnd();
                BI_RequestInitializatorSimulation();
                return 0;
            }
        });
    }

    public void BI_RequestInitializatorSimulation() {
        ArrayList<Agent> agentsList = new ArrayList<>(
                DF_HELPER.getAgentsList(this.agentConfig.COLLECTION_PLACE_CONFIG));
        ACLMessage msg = DF_HELPER.ACL_MESSAGE_REQUEST_INITIALIZATOR_SIMULATION;
        msg.clearAllReceiver();
        for (Agent agent : agentsList) {
            msg.addReceiver(agent.getAID());
        }
        doWait(1000);
        addBehaviour(new ContractNetInitiator(this, msg) {
            String requestInitializatorName;

            // @Override
            // protected void handlePropose(ACLMessage propose, Vector acceptances) {
            //     System.out.println("LN179");
            // }

            protected void handleAllResponses(Vector responses, Vector acceptances) {
                ArrayList<ACLMessage> responsesList = new ArrayList<ACLMessage>(responses);
                responsesList.removeIf(message -> message.getPerformative() != ACLMessage.PROPOSE);
                if (!responsesList.isEmpty()) {
                    orderInitializatorsSimulators(responsesList, ParametersConfig.ASC_STRING);
                    for (ACLMessage aclMessage : responsesList) {
                        ACLMessage reply = aclMessage.createReply();
                        if (responsesList.indexOf(aclMessage) == 0) {
                            this.requestInitializatorName = aclMessage.getSender().getLocalName();
                            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        } else {
                            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        }
                        acceptances.addElement(reply);
                    }
                } else {
                    DF_HELPER.println(myAgent, "NO HAY LUGARES DE ACOPIO DISPONIBLES");
                    System.exit(0);
                }
            }

            protected void handleInform(ACLMessage inform) {
                DF_HELPER.println(this.getAgent(), inform);
            }

            @Override
            public int onEnd() {
                super.onEnd();
                BI_InitializeSimulation(requestInitializatorName);
                return 0;
            }
        });

    }

    public void BI_InitializeSimulation(String nameAgent) {
        ACLMessage msg = DF_HELPER.ACL_MESSAGE_INITIALIZE_SIMULATION;
        msg.addReceiver(DF_HELPER.getAgent(nameAgent).getAID());
        msg.setContent(Long.toString((long) this.currExecutionTime));
        currCreationScenarioConfig.addNCurrIteration();
        send(msg);
    }

    public void BR_EndSimulation() {
        MessageTemplate template = DF_HELPER.MESSAGE_TEMPLATE_END_SIMULATION;
        // MessageTemplate template = DF_HELPER.MESSAGE_TEMPLATE_INITIALIZE_SIMULATION;
        this.addBehaviour(new SimpleResponder(this, template) {
            CreationScenarioConfig currCreationScenario;
            ScenarioConfig scenarioConfig;

            public void setValues() {
                scenarioConfig = getScenarioConfig();
                currCreationScenario = getCurrCreationScenarioConfig();
            }

            @Override
            protected void handleAclMessage(ACLMessage msg) {
                DF_HELPER.println(myAgent, msg);
                setValues();
                String jsonContent = msg.getContent();
                ArrayList<SupplyActivity> supplyActivitiesListResponseArrayList = new ArrayList<>(Arrays
                        .asList(new Gson().fromJson(jsonContent, SupplyActivity[].class)));
                System.out.println(getCurrCreationScenarioConfig().getSupplyActivitiesList().size());
                if (currCreationScenario.getIterationsPending()) {
                    DF_HELPER.println("SIGUIENTE ITERACION");
                    BI_UpdateTimeEvent();
                } else {
                    currCreationScenario.setStateIteration(ParametersConfig.STATE_SCENARIO_CONFIG_END);
                    currCreationScenario.setSupplyActivitiesList(supplyActivitiesListResponseArrayList);
                    CreationScenarioConfig nextCreationScenario = scenarioConfig
                            .getNextCreationScenarioConfigEnable(ParametersConfig.STATE_SCENARIO_CONFIG_NOT_INITIALIZE);
                    if (Objects.nonNull(nextCreationScenario)) {
                        DF_HELPER.println("SIGUIENTE ESCENARIO " + nextCreationScenario.getName());
                        currCreationScenario = nextCreationScenario;
                        F_UpdateState();
                    } else {
                        DF_HELPER.println("NO QUEDAN MAS ESCENARIO");
                        F_PrintResults();
                    }
                }
            }
        });

        // this.addBehaviour(new SimpleResponder(this, template) {
        //     CreationScenarioConfig currCreationScenario;
        //     ScenarioConfig scenarioConfig;

        //     public void setValues() {
        //         scenarioConfig = getScenarioConfig();
        //         currCreationScenario = getCurrCreationScenarioConfig();
        //     }

        //     @Override
        //     protected void handleAclMessage(ACLMessage msg) {
        //         DF_HELPER.println(myAgent, msg);
        //         setValues();
        //         String jsonContent = msg.getContent();
        //         ArrayList<SupplyActivity> supplyActivitiesListResponseArrayList = new ArrayList<>(Arrays
        //                 .asList(new Gson().fromJson(jsonContent, SupplyActivity[].class)));
        //         System.out.println(getCurrCreationScenarioConfig().getSupplyActivitiesList().size());
        //         if (currCreationScenario.getIterationsPending()) {
        //             DF_HELPER.println("SIGUIENTE ITERACION");
        //             BI_UpdateTimeEvent();
        //         } else {
        //             currCreationScenario.setStateIteration(ParametersConfig.STATE_SCENARIO_CONFIG_END);
        //             currCreationScenario.setSupplyActivitiesList(supplyActivitiesListResponseArrayList);
        //             CreationScenarioConfig nextCreationScenario = scenarioConfig
        //                     .getNextCreationScenarioConfigEnable(ParametersConfig.STATE_SCENARIO_CONFIG_NOT_INITIALIZE);
        //             if (Objects.nonNull(nextCreationScenario)) {
        //                 DF_HELPER.println("SIGUIENTE ESCENARIO " + nextCreationScenario.getName());
        //                 currCreationScenario = nextCreationScenario;
        //                 F_UpdateState();
        //             } else {
        //                 DF_HELPER.println("NO QUEDAN MAS ESCENARIO");
        //                 F_PrintResults();
        //             }
        //         }
        //         // BI_ConsultRequiredSupply();
        //     }
        // });
    }

    public void orderInitializatorsSimulators(ArrayList<ACLMessage> responsesList, String type) {
        Collections.sort(responsesList, new Comparator<ACLMessage>() {
            @Override
            public int compare(ACLMessage msg1, ACLMessage msg2) {
                int value1 = Integer.parseInt(msg1.getContent());
                int value2 = Integer.parseInt(msg2.getContent());
                return type.equals(ParametersConfig.ASC_STRING) ? Integer.compare(value1, value2)
                        : Integer.compare(value2, value1);
            }
        });
    }

    public void F_PrintResults() {
        DF_HELPER.println("PRINT RESULTS");
        FileGenerator fileteGenerator = new FileGenerator();
        for (CreationScenarioConfig creationScenarioConfig : this.scenarioConfig.getCreationScenarioConfigList()) {
            System.out.println(creationScenarioConfig);
            System.out.println(creationScenarioConfig.getSupplyActivitiesList().size());
            System.out.println("----");
            fileteGenerator.generateFile(creationScenarioConfig.getName(),
                    creationScenarioConfig.getSupplyActivitiesList());
        }
    }

    public void BR_CreacionFinalizada() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId(DF_HELPER.IC_FINISHED_CREATION));
        addBehaviour(new AchieveREResponder(this, template) {
            AgentConfig agentConfig;
            CreationAgentConfig currObj = null;

            public void setValues() {
                agentConfig = getAgentConfig();
            }

            @Override
            protected ACLMessage handleRequest(ACLMessage request) {
                setValues();
                ACLMessage reply = request.createReply();
                System.out.println("Mensaje recibido de " + request.getSender().getLocalName());
                Integer tipo = ACLMessage.AGREE;
                // Integer numberEnabled = 0, initialCreated = 0;
                if (Objects.equals(request.getContent(), agentConfig.TRANSPORTER_CONFIG.getClassName())) {
                    currObj = agentConfig.TRANSPORTER_CONFIG;
                } else if (Objects.equals(request.getContent(), agentConfig.TRUCK_CONFIG.getClassName())) {
                    currObj = agentConfig.TRUCK_CONFIG;
                } else if (Objects.equals(request.getContent(),
                        agentConfig.COLLECTION_PLACE_CONFIG.getClassName())) {
                    currObj = agentConfig.COLLECTION_PLACE_CONFIG;
                } else if (Objects.equals(request.getContent(),
                        agentConfig.DISTRIBUTION_AREA_CONFIG.getClassName())) {
                    currObj = agentConfig.DISTRIBUTION_AREA_CONFIG;
                } else if (Objects.equals(request.getContent(), agentConfig.DONOR_CONFIG.getClassName())) {
                    currObj = agentConfig.DONOR_CONFIG;
                } else {
                    System.out.println("No deberia ir acá");
                    System.exit(-1);
                }
                reply.setPerformative(tipo);
                currObj.setNumCurrAgentsCreation(this.currObj.getNumCurrAgentsCreation() + 1);
                return reply;
            }

            @Override
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
                if (Objects.equals(currObj.getNumInFileAgents(), currObj.getNumCurrAgentsCreation())) {
                    System.out.println("Creación de agentes finalizados para " + currObj.getClassName());
                    currObj.setEnabledIterationBoolean(false);
                    loadDataGenerateAgent();
                }
                return null;
            }

        });
    }

    public void crearAgentesTransporte() {
        CreationAgentConfig configObject = agentConfig.TRANSPORTER_CONFIG;
        // ContainerController containerObject = TRANSPORTER_CONTAINER_CONTROLLER;
        try (CSVReader reader = new CSVReader(new FileReader(configObject.getFileRoute()))) {
            ArrayList<String> dataFile = reader.readAll().stream()
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toCollection(ArrayList::new));
            configObject.setNumInFileAgents(dataFile.size() - 1);
            Boolean header = true;
            for (String value : dataFile) {
                if (header) {
                    header = false;
                    continue;
                }
                ArrayList<String> datos = new ArrayList<>(Arrays.asList(value.split(";")));
                configObject.getContainerController()
                        .createNewAgent(datos.get(0), configObject.getClassRoute(), new Object[] { datos.get(2) })
                        .start();
            }
        } catch (IOException | StaleProxyException | CsvException e) {
            e.printStackTrace();
        }
    }

    public void crearAgentesCamiones() {
        CreationAgentConfig configObject = agentConfig.TRUCK_CONFIG;
        try (CSVReader reader = new CSVReader(new FileReader(configObject.getFileRoute()))) {
            ArrayList<String> dataFile = reader.readAll().stream()
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toCollection(ArrayList::new));
            configObject.setNumInFileAgents(dataFile.size() - 1);
            Boolean header = true;
            ArrayList<JsonObject> trucksData = new ArrayList<>();
            Integer n_columns = 3;
            JsonObject jsonObject;
            for (String value : dataFile) {
                if (header) {
                    ArrayList<String> datos = new ArrayList<>(Arrays.asList(value.split(";")));
                    for (int i = n_columns; i < datos.size(); i++) {
                        // Dividir el string en dos partes por el carácter "_"
                        String[] data = datos.get(i).split("_");
                        // La primera parte permanece igual
                        String name = data[0];
                        // La segunda parte contiene solo los dígitos numéricos
                        String capacity = data[1].replaceAll("[^0-9]", "");
                        jsonObject = new JsonObject();
                        jsonObject.addProperty("name", name);
                        jsonObject.addProperty("capacity", capacity);
                        trucksData.add(jsonObject);
                    }
                    System.out.println(trucksData);
                    header = false;
                    continue;
                }
                ArrayList<String> datos = new ArrayList<>(Arrays.asList(value.split(";")));
                for (int i = n_columns; i < datos.size(); i++) {
                    var carrierName = datos.get(0);
                    var initialUbicationLat = datos.get(1);
                    var initialUbicationLon = datos.get(2);
                    for (int j = 1; j <= Integer.parseInt(datos.get(i)); j++) {
                        String name = trucksData.get(i - n_columns).get("name").getAsString();
                        int capacity = trucksData.get(i - n_columns).get("capacity").getAsInt();
                        var nameTruck = carrierName + "_" + name + "_"
                                + Integer.toString(j);
                        configObject.getContainerController()
                                .createNewAgent(nameTruck, configObject.getClassRoute(),
                                        new Object[] { carrierName, initialUbicationLat, initialUbicationLon,
                                                capacity })
                                .start();
                    }
                }
            }
        } catch (IOException | CsvException | StaleProxyException e) {
            e.printStackTrace();
        }
    }

    public void crearAgentesPuntoDistribucion() {
        CreationAgentConfig configObject = agentConfig.DISTRIBUTION_AREA_CONFIG;

        try (CSVReader reader = new CSVReader(new FileReader(configObject.getFileRoute()))) {
            ArrayList<String> dataFile = reader.readAll().stream()
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toCollection(ArrayList::new));
            configObject.setNumInFileAgents(dataFile.size() - 1);
            Boolean header = true;
            for (String value : dataFile) {
                if (header) {
                    header = false;
                    continue;
                }
                ArrayList<String> datos = new ArrayList<>(Arrays.asList(value.split(";")));
                configObject.getContainerController()
                        .createNewAgent(datos.get(0), configObject.getClassRoute(), new Object[] {
                                // POBLACION
                                datos.get(1),
                                // UBICACION
                                datos.get(2), datos.get(3),
                                // STOCK
                                datos.get(4), datos.get(5), datos.get(6), datos.get(7), datos.get(8), datos.get(9) })
                        .start();
            }
        } catch (IOException | CsvException | StaleProxyException e) {
            e.printStackTrace();
        }
    }

    public void crearAgentesDonador() {
        CreationAgentConfig configObject = agentConfig.DONOR_CONFIG;
        try (CSVReader reader = new CSVReader(new FileReader(configObject.getFileRoute()))) {
            ArrayList<String> dataFile = reader.readAll().stream()
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toCollection(ArrayList::new));
            configObject.setNumInFileAgents(dataFile.size() - 1);
            Boolean header = true;
            for (String value : dataFile) {
                if (header) {
                    header = false;
                    continue;
                }
                ArrayList<String> datos = new ArrayList<>(Arrays.asList(value.split(";")));
                configObject.getContainerController()
                        .createNewAgent(datos.get(0), configObject.getClassRoute(), new Object[] {
                                // UBICACION
                                datos.get(1), datos.get(2),
                                // STOCK
                                datos.get(3), datos.get(4), datos.get(5), datos.get(6), datos.get(7), datos.get(8) })
                        .start();
            }
        } catch (IOException | CsvException | StaleProxyException e) {
            e.printStackTrace();
        }
    }

    public void crearAgentesLugarAcopio() {
        CreationAgentConfig configObject = agentConfig.COLLECTION_PLACE_CONFIG;
        try (CSVReader reader = new CSVReader(new FileReader(configObject.getFileRoute()))) {
            ArrayList<String> dataFile = reader.readAll().stream()
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toCollection(ArrayList::new));
            configObject.setNumInFileAgents(dataFile.size() - 1);
            Boolean header = true;
            for (String value : dataFile) {
                if (header) {
                    header = false;
                    continue;
                }
                ArrayList<String> datos = new ArrayList<>(Arrays.asList(value.split(";")));
                configObject.getContainerController()
                        .createNewAgent(datos.get(0), configObject.getClassRoute(), new Object[] {
                                // UBICACION
                                datos.get(1), datos.get(2),
                                // STOCK
                                datos.get(3), datos.get(4), datos.get(5), datos.get(6), datos.get(7), datos.get(8) })
                        .start();
            }
        } catch (IOException | CsvException | StaleProxyException e) {
            e.printStackTrace();
        }
    }

    public DFHelper getDF_HELPER() {
        return DF_HELPER;
    }

    public long getCurrExecutionTime() {
        return currExecutionTime;
    }

    public void setCurrExecutionTime(long currExecutionTime) {
        this.currExecutionTime = currExecutionTime;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public CreationScenarioConfig getCurrCreationScenarioConfig() {
        return currCreationScenarioConfig;
    }

    public void setCurrCreationScenarioConfig(CreationScenarioConfig currCreationScenarioConfig) {
        this.currCreationScenarioConfig = currCreationScenarioConfig;
    }

    public ScenarioConfig getScenarioConfig() {
        return scenarioConfig;
    }

    public AgentConfig getAgentConfig() {
        return agentConfig;
    }
}
