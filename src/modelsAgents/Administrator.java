package src.modelsAgents;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;
import jade.wrapper.StaleProxyException;
import src.commons.AgentConfig;
import src.commons.FileGenerator;
import src.commons.ParametersConfig;
import src.commons.ScenarioConfig;
import src.models.SupplyActivity;
import src.commons.ScenarioConfig.BehaviourCreationScenarioConfig;
import src.commons.ScenarioConfig.CreationScenarioConfig;
import src.commons.AgentConfig.CreationAgentConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;



public class Administrator extends Agent {
    private final DFHelper DF_HELPER = DFHelper.getInstance();
    private long currExecutionTime;
    private boolean first = true;
    private CreationScenarioConfig currCreationScenarioConfig = null;
    private ArrayList<SupplyActivity> supplyActivitiesList = new ArrayList<>();

    public ArrayList<SupplyActivity> getSupplyActivitiesList() {
        return supplyActivitiesList;
    }

    public void setSupplyActivitiesList(ArrayList<SupplyActivity> supplyActivitiesList) {
        this.supplyActivitiesList = supplyActivitiesList;
    }

    @Override
    protected void setup() {
        DF_HELPER.registrarServicio(this);
        this.BR_CreacionFinalizada();
        this.BR_EndSimulation();
        this.loadDataGenerateAgent();
    }

    public void loadDataGenerateAgent() {
        CreationAgentConfig currAgentConfig = AgentConfig.getNextCreationAgentConfigEnable();
        if (!Objects.isNull(currAgentConfig)) {
            if (Objects.equals(currAgentConfig.getClassName(), AgentConfig.TRANSPORTER_CONFIG.getClassName())) {
                this.crearAgentesTransporte();
            } else if (Objects.equals(currAgentConfig.getClassName(), AgentConfig.TRUCK_CONFIG.getClassName())) {
                this.crearAgentesCamiones();
            } else if (Objects.equals(currAgentConfig.getClassName(), AgentConfig.DONOR_CONFIG.getClassName())) {
                this.crearAgentesDonador();
            } else if (Objects.equals(currAgentConfig.getClassName(),
                    AgentConfig.DISTRIBUTION_AREA_CONFIG.getClassName())) {
                this.crearAgentesPuntoDistribucion();
            } else if (Objects.equals(currAgentConfig.getClassName(),
                    AgentConfig.COLLECTION_PLACE_CONFIG.getClassName())) {
                this.crearAgentesLugarAcopio();
            } else {
                System.out.println("Error");
                System.exit(0);
            }
        } else {
            AgentConfig.enableCreationConfigList();
            this.currCreationScenarioConfig = ScenarioConfig
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
        // if (Objects.nonNull(enabBoolean)){

        List<Agent> listAgents = enabBoolean ? listAgentsEnable : listAgentsDisable;
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setConversationId(DF_HELPER.IC_UPDATE_AGENT_STATE);
        msg.setContent(Boolean.toString(enabBoolean));
        for (Agent agent : listAgents) {
            msg.addReceiver(agent.getAID());
        }

        this.addBehaviour(new AchieveREInitiator(this, msg) {
            @Override
            protected void handleAllResultNotifications(Vector notifications) {
                DF_HELPER.println(myAgent, "Todos los agentes realizaron la actualización de su estado");
            }

            @Override
            public int onEnd() {
                super.onEnd();
                if (enabBoolean) {
                    BI_UpdateState(listAgentsEnable, listAgentsDisable, false);
                } else {
                    BI_InitializeSimulation();
                }
                return 0;
            }
        });
    }

    public void BI_InitializeSimulation() {
        Agent agente = DF_HELPER.getAgentsList(AgentConfig.COLLECTION_PLACE_CONFIG)
                .get(0);
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        if (this.currCreationScenarioConfig.getnCurrIterations() == 0) {
            currExecutionTime = ParametersConfig.TIME_EXECUTION_INIT;
        } else {
            currExecutionTime += ParametersConfig.TIME_EXECUTION_ADD;
        }
        msg.setConversationId(DF_HELPER.IC_INITIALIZE_SIMULATION);
        msg.addReceiver(agente.getAID());
        msg.setContent(Long.toString((long) currExecutionTime));
        currCreationScenarioConfig.addNCurrIteration();
        this.addBehaviour(new AchieveREInitiator(this, msg) {
            @Override
            protected void handleAgree(ACLMessage agree) {
                DF_HELPER.println(myAgent, "Llega mensaje que comienza la gestión de cadena de suministros");
            }

            @Override
            protected void handleRefuse(ACLMessage refuse) {
                DF_HELPER.println(myAgent, "NO HAY LUGARES DE ACOPIO DISPONIBLES");
                System.exit(0);
            }
        });
    }

    public void BR_EndSimulation() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId(DF_HELPER.IC_END_SIMULATION));
        this.addBehaviour(new ContractNetResponder(this, template) {
            protected ACLMessage handleCfp(ACLMessage cfp) {
                String jsonContent = cfp.getContent();
                ArrayList<SupplyActivity> supplyActivitiesList = getSupplyActivitiesList();
                ArrayList<SupplyActivity> supplyActivitiesListResponseArrayList = new ArrayList<>(Arrays
                        .asList(new Gson().fromJson(jsonContent, SupplyActivity[].class)));
                supplyActivitiesList.addAll(supplyActivitiesListResponseArrayList);
                CreationScenarioConfig currCreationScenario = getCurrCreationScenarioConfig();
                if (currCreationScenario.getIterationsPending()) {
                    DF_HELPER.println("SIGUIENTE ITERACION");
                    BI_InitializeSimulation();
                } else {
                    currCreationScenario.setStateIteration(ParametersConfig.STATE_SCENARIO_CONFIG_END);
                    CreationScenarioConfig nextCreationScenario = ScenarioConfig
                            .getNextCreationScenarioConfigEnable(ParametersConfig.STATE_SCENARIO_CONFIG_NOT_INITIALIZE);
                    if (Objects.nonNull(nextCreationScenario)) {
                        DF_HELPER.println("SIGUIENTE ESCENARIO");
                        setCurrCreationScenarioConfig(nextCreationScenario);
                        F_UpdateState();
                    } else {
                        DF_HELPER.println("NO QUEDAN MAS ESCENARIO");
                        F_PrintResults();
                    }
                }
                return null;
            }
        });
    }

    public void F_PrintResults() {
        DF_HELPER.println("PRINT RESULTS");
        FileGenerator fileteGenerator = new FileGenerator();
        fileteGenerator.generateFile(this.getCurrCreationScenarioConfig().getName(), supplyActivitiesList);
        System.exit(0);
    }

    public void BR_CreacionFinalizada() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId(DF_HELPER.IC_FINISHED_CREATION));
        addBehaviour(new AchieveREResponder(this, template) {
            CreationAgentConfig currObj = null;

            @Override
            protected ACLMessage handleRequest(ACLMessage request) {
                ACLMessage reply = request.createReply();
                System.out.println("Mensaje recibido de " + request.getSender().getLocalName());
                Integer tipo = ACLMessage.AGREE;
                // Integer numberEnabled = 0, initialCreated = 0;
                if (Objects.equals(request.getContent(), AgentConfig.TRANSPORTER_CONFIG.getClassName())) {
                    currObj = AgentConfig.TRANSPORTER_CONFIG;
                } else if (Objects.equals(request.getContent(), AgentConfig.TRUCK_CONFIG.getClassName())) {
                    currObj = AgentConfig.TRUCK_CONFIG;
                } else if (Objects.equals(request.getContent(), AgentConfig.COLLECTION_PLACE_CONFIG.getClassName())) {
                    currObj = AgentConfig.COLLECTION_PLACE_CONFIG;
                } else if (Objects.equals(request.getContent(), AgentConfig.DISTRIBUTION_AREA_CONFIG.getClassName())) {
                    currObj = AgentConfig.DISTRIBUTION_AREA_CONFIG;
                } else if (Objects.equals(request.getContent(), AgentConfig.DONOR_CONFIG.getClassName())) {
                    currObj = AgentConfig.DONOR_CONFIG;
                } else {
                    System.out.println("No deberia ir acá");
                    System.exit(-1);
                }
                reply.setPerformative(tipo);
                currObj.setNumCurrAgentsCreation(currObj.getNumCurrAgentsCreation() + 1);
                return reply;
            }

            @Override
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
                if (Objects.equals(currObj.getNumInFileAgents(), currObj.getNumCurrAgentsCreation())) {
                    System.out.println("Creación de agentes finalizados para " + currObj.getClassName());
                    currObj.setDisableIterationBoolean();
                    loadDataGenerateAgent();
                }
                return null;
            }

        });
    }

    public void crearAgentesTransporte() {
        AgentConfig.CreationAgentConfig configObject = AgentConfig.TRANSPORTER_CONFIG;
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
        AgentConfig.CreationAgentConfig configObject = AgentConfig.TRUCK_CONFIG;
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
        AgentConfig.CreationAgentConfig configObject = AgentConfig.DISTRIBUTION_AREA_CONFIG;

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
        AgentConfig.CreationAgentConfig configObject = AgentConfig.DONOR_CONFIG;
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
        AgentConfig.CreationAgentConfig configObject = AgentConfig.COLLECTION_PLACE_CONFIG;
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
}
