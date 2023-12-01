package src.modelsAgents;

import java.io.Console;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils.Null;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.lang.acl.ACLMessage;
import jade.core.Runtime;
import jade.core.event.AgentEvent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetInitiator;
import jade.proto.SimpleAchieveREResponder;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import src.commons.AgentConfig;
import src.commons.ParametersConfig;
import src.commons.ScenarioConfig;
import src.commons.TestConfig;
import src.commons.ScenarioConfig.BehaviourCreationScenarioConfig;
import src.commons.ScenarioConfig.CreationScenarioConfig;
import src.commons.AgentConfig.CreationAgentConfig;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

public class Administrator extends Agent {
    private final DFHelper DF_HELPER = DFHelper.getInstance();
    private long currExecutionTime = 0;
    private boolean first = true;

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
            this.F_UpdateState();
        }
    }

    public void F_UpdateState() {
        CreationScenarioConfig currCreationScenarioConfig = null;
        // HAY QUE HACER LA VARIABLE QUE PUEDA CREAR LOS EVENTOS ALEATORIOS
        Boolean runEvent = true;
        if (runEvent) {
            currCreationScenarioConfig = ScenarioConfig.getNextCreationScenarioConfigEnable();
            if (Objects.nonNull(currCreationScenarioConfig)) {
                System.out.println("----PROCESS CHANGE STATE OF AGENTS----" + currCreationScenarioConfig.getName());
                ArrayList<Agent> listAgentsEnable = new ArrayList<>();
                ArrayList<Agent> listAgentsDisable = new ArrayList<>();

                for (BehaviourCreationScenarioConfig currBehaviourCreationScenarioConfig : currCreationScenarioConfig
                        .getBehaviourCreationScnearionConfigList()) {
                    AtomicInteger nAgents = new AtomicInteger(0);
                    for (Agent agent : DF_HELPER
                            .getAgentsList(currBehaviourCreationScenarioConfig.getCreationAgentConfig())) {
                        (nAgents.incrementAndGet() <= currBehaviourCreationScenarioConfig.getnEnabledAgents()
                                ? listAgentsEnable
                                : listAgentsDisable).add(agent);
                    }
                    currBehaviourCreationScenarioConfig.setDisable();
                }
                currCreationScenarioConfig.setDisable();
                BI_UpdateState(listAgentsEnable, listAgentsDisable, true);
            } else {
                System.out.println("----END OF THE EVENTS----");
                System.exit(0);
                // System.out.println("No quedan currCreation");

            }
        }

    }

    public void BI_UpdateState(ArrayList<Agent> listAgentsEnable, ArrayList<Agent> listAgentsDisable,
            Boolean enabBoolean) {
        // if (Objects.nonNull(enabBoolean)){
        ArrayList<Agent> listAgents = enabBoolean ? listAgentsEnable : listAgentsDisable;
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setConversationId(DF_HELPER.IC_UPDATE_AGENT_STATE);
        msg.setContent(Boolean.toString(enabBoolean));
        for (Agent agent : listAgents) {
            msg.addReceiver(agent.getAID());
        }

        this.addBehaviour(new AchieveREInitiator(this, msg) {
            @Override
            protected void handleAllResultNotifications(Vector notifications) {
                System.out.println("Llegaron todos los mensajes");
            }

            @Override
            public int onEnd() {
                super.onEnd();
                if (enabBoolean) {
                    BI_UpdateState(listAgentsEnable, listAgentsDisable, false);
                } else {
                    BI_InitializeSimulation(null);
                }
                return 0;
            }
        });
    }

    public void BI_InitializeSimulation(Integer numAgentInitializator) {
        final AtomicInteger finalNumAgentInitializator = new AtomicInteger(
                Objects.isNull(numAgentInitializator) ? 0 : numAgentInitializator);
        Agent agente = DF_HELPER.getAgentsList(AgentConfig.COLLECTION_PLACE_CONFIG)
                .get(finalNumAgentInitializator.get());
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        if (!first) {
            currExecutionTime = currExecutionTime + ParametersConfig.ADD_TIME_EXECUTION;
        }
        msg.setConversationId(DF_HELPER.IC_INITIALIZE_SIMULATION);
        msg.addReceiver(agente.getAID());
        msg.setContent(Integer.toString((int) currExecutionTime));
        this.addBehaviour(new AchieveREInitiator(this, msg) {
            @Override
            protected void handleAgree(ACLMessage agree) {
                System.out.println(
                        "Iniciador: Llega mensaje en que hay un lugar de acopio habilitado para comenzar la cadena de suministros");
            }

            @Override
            protected void handleRefuse(ACLMessage refuse) {
                System.out.println(finalNumAgentInitializator.incrementAndGet());
                if (finalNumAgentInitializator.get() < DF_HELPER.getAgentsList(AgentConfig.COLLECTION_PLACE_CONFIG)
                        .size()) {
                    BI_InitializeSimulation(finalNumAgentInitializator.get());
                } else {
                    System.out.println("No hay lugares de acopio disponibles");
                    F_UpdateState();
                }
            }

            // protected void handleFailure(ACLMessage failure) {
            // System.out.println("Iniciador: Mensaje FAILURE recibido del Responder");
            // }

            // protected void handleNotUnderstood(ACLMessage notUnderstood) {
            // System.out.println("Iniciador: Mensaje NOT_UNDERSTOOD recibido del
            // Responder");
            // }

            // protected void handleOutOfSequence(ACLMessage msg) {
            // System.out.println("Iniciador: Mensaje OUT_OF_SEQUENCE recibido del
            // Responder");
            // }

            // protected void handleAllResponses(Vector responses) {
            // System.out.println("Iniciador: Todas las respuestas recibidas del
            // Responder");
            // // Realizar alguna acción después de recibir todas las respuestas
            // // ...
            // }

            // protected void handleAllResultNotifications(Vector resultNotifications) {
            // System.out.println("Iniciador: Todas las notificaciones de resultado
            // recibidas del Responder");
            // // Realizar alguna acción después de recibir todas las notificaciones de
            // // resultado
            // // ...
            // }
        });

        // this.addBehaviour(new AchieveREInitiator(this, msg) {
        // protected void handleInform(ACLMessage inform) {
        // System.out.println("Iniciador: Mensaje de respuesta recibido del receptor");
        // // Enviar la confirmación al receptor
        // ACLMessage confirmacion = inform.createReply();
        // confirmacion.setPerformative(ACLMessage.CONFIRM);
        // send(confirmacion);
        // }

        // @Override
        // protected void handleAgree(ACLMessage agree) {
        // System.out.println("----Recibe mensaje que inicia la simulación----");
        // ACLMessage confirmacion = agree.createReply();
        // confirmacion.setPerformative(ACLMessage.CONFIRM);
        // send(confirmacion);
        // }

        // @Override
        // protected void handleRefuse(ACLMessage refuse) {
        // System.out.println(finalNumAgentInitializator.incrementAndGet());
        // if (finalNumAgentInitializator.get() <
        // DF_HELPER.getAgentsList(AgentConfig.COLLECTION_PLACE_CONFIG)
        // .size()) {
        // BI_InitializeSimulation(finalNumAgentInitializator.get());
        // } else {
        // System.out.println("No hay lugares de acopio disponibles");
        // F_UpdateState();
        // }
        // }
        // });

    }

    public void BR_EndSimulation() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId(DF_HELPER.IC_END_SIMULATION));

        this.addBehaviour(new AchieveREResponder(this, template) {
            @Override
            protected ACLMessage prepareResponse(ACLMessage request) {
                return null; // No se envía ninguna respuesta al mensaje de solicitud
            }

            @Override
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
                System.out.println("Se recibe mensaje de fin de simulación");
                // No se retorna ningún mensaje de notificación como resultado
                return null;
            }
            // protected void handleInform(ACLMessage inform) {
            // System.out.println("NotificationResponderAgent: INFORM received from " +
            // inform.getSender().getName());
            // System.out.println("Content: " + inform.getContent());
            // // Aquí puedes implementar la lógica para procesar la notificación recibida
            // }
        });
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
                                        new Object[] { carrierName, initialUbicationLat, initialUbicationLon, capacity })
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
}
