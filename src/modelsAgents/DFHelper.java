package src.modelsAgents;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.transform.Templates;

import jade.core.Agent;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.SimpleAchieveREResponder;
import jade.proto.SubscriptionResponder;
import jade.tools.sniffer.Message;
import src.behaviours.SimpleConversationResponder;
// import src.commons.ContainerAgentConfig;
import src.commons.ContainerAgentConfig;
// import src.commons.ContainerAgentConfig.CreationAgentConfig;
import src.commons.CreationAgentConfig;

public class DFHelper extends Agent {
    private static DFHelper instance = null;
    public final ArrayList<Administrator> LIST_REGISTERED_ADMINISTRATOR = new ArrayList<>();
    public final ArrayList<Truck> LIST_REGISTERED_TRUCK = new ArrayList<>();
    public final ArrayList<Transporter> LIST_REGISTERED_TRANSPORTER = new ArrayList<>();
    public final ArrayList<DistributionArea> LIST_REGISTERED_DISTRIBUTION_AREA = new ArrayList<>();
    public final ArrayList<Donor> LIST_REGISTERED_DONOR = new ArrayList<>();
    public final ArrayList<CollectionPlace> LIST_REGISTERED_COLLECTION_PLACE = new ArrayList<>();

    /*
     * IC - ID CONVERSATIONS
     */
    public final String IC_FINISHED_CREATION = "ID_CONVERSATION_FINISHED_CREATION";
    // public final String IC_UPDATE_AGENT_STATE =
    // "ID_CONVERSATION_UPDATE_AGENT_STATE";
    // public final String IC_END_SIMULATION = "ID_CONVERSATION_END_SIMULATION";
    public final String IC_CONSULT_REQUIRED_SUPPLY = "ID_CONVERSATION_CONSULT_REQUIRED_SUPPLY";
    public final String IC_CONSULT_PROPOSED_SUPPLY = "ID_CONVERSATION_CONSULT_PROPOSED_SUPPLY";
    public final String IC_CONSULT_TRANSPORTER = "ID_CONVERSATION_CONSULT_TRANSPORT";
    public final String IC_REQUEST_FREIGHT = "ID_CONVERSATION_CONSULT_FREIGHT";
    public final String IC_REGISTER_TO_CARRIER = "ID_CONVERSATION_REGISTER_TO_CARRIER";
    public final String IC_CONFIRM_SUPPLY_ACTIVITY = "ID_CONVERSATION_CONFIRM_SUPPLY_ACTIVITY";

    // REQUEST_INITIALIZATOR_SIMULATION
    private final int INT_MESSAGE_REQUEST_INITIALIZATOR_SIMULATION = ACLMessage.CFP;
    private final String IC_REQUEST_INITIALIZATOR_SIMULATION = "ID_CONVERSATION_REQUEST_INITIALIZATOR_SIMULATION";
    public final ACLMessage ACL_MESSAGE_REQUEST_INITIALIZATOR_SIMULATION = getMessage(
            INT_MESSAGE_REQUEST_INITIALIZATOR_SIMULATION, IC_REQUEST_INITIALIZATOR_SIMULATION);
    public final MessageTemplate MESSAGE_TEMPLATE_REQUEST_INITIALIZATOR_SIMULATION = getMessageTemplate(
            INT_MESSAGE_REQUEST_INITIALIZATOR_SIMULATION, IC_REQUEST_INITIALIZATOR_SIMULATION);

    // INITIALIZE_SIMULATION
    private final int INT_MESSAGE_INITIALIZE_SIMULATION = ACLMessage.REQUEST;
    private final String IC_INITIALIZE_SIMULATION = "ID_CONVERSATION_INITIALIZE_SIMULATION";
    public final ACLMessage ACL_MESSAGE_INITIALIZE_SIMULATION = getMessage(
            INT_MESSAGE_INITIALIZE_SIMULATION, IC_INITIALIZE_SIMULATION);
    public final MessageTemplate MESSAGE_TEMPLATE_INITIALIZE_SIMULATION = getMessageTemplate(
            INT_MESSAGE_INITIALIZE_SIMULATION, IC_INITIALIZE_SIMULATION);

    // UPDATE_AGENT_STATE
    private final int INT_MESSAGE_UPDATE_AGENT_STATE = ACLMessage.REQUEST;
    private final String IC_UPDATE_AGENT_STATE = "ID_CONVERSATION_UPDATE_AGENT_STATE";
    public final ACLMessage ACL_MESSAGE_UPDATE_AGENT_STATE = getMessage(
            INT_MESSAGE_UPDATE_AGENT_STATE, IC_UPDATE_AGENT_STATE);
    public final MessageTemplate MESSAGE_TEMPLATE_UPDATE_AGENT_STATE = getMessageTemplate(
            INT_MESSAGE_UPDATE_AGENT_STATE, IC_UPDATE_AGENT_STATE);

    // UPDATE_TIME_EVENT
    private final int INT_MESSAGE_UPDATE_TIME_EVENT = ACLMessage.REQUEST;
    private final String IC_UPDATE_TIME_EVENT = "ID_CONVERSATION_UPDATE_TIME_EVENT";
    public final ACLMessage ACL_MESSAGE_UPDATE_TIME_EVENT = getMessage(
            INT_MESSAGE_UPDATE_TIME_EVENT, IC_UPDATE_TIME_EVENT);
    public final MessageTemplate MESSAGE_TEMPLATE_UPDATE_TIME_EVENT = getMessageTemplate(
            INT_MESSAGE_UPDATE_TIME_EVENT, IC_UPDATE_TIME_EVENT);

    // REINITIALIZE_DATA
    final int INT_MESSAGE_REINITIALIZE_DATA = ACLMessage.REQUEST;
    private final String IC_REINITIALIZE_DATA = "ID_CONVERSATION_REINITIALIZE_DATA";
    public final ACLMessage ACL_MESSAGE_REINITIALIZE_DATA = getMessage(
            INT_MESSAGE_REINITIALIZE_DATA, IC_REINITIALIZE_DATA);
    public final MessageTemplate MESSAGE_TEMPLATE_REINITIALIZE_DATA = getMessageTemplate(
            INT_MESSAGE_REINITIALIZE_DATA, IC_REINITIALIZE_DATA);

    // END_SIMULATION
    private final int INT_MESSAGE_END_SIMULATION = ACLMessage.REQUEST;
    private final String IC_END_SIMULATION = "ID_CONVERSATION_END_SIMULATION";
    public final ACLMessage ACL_MESSAGE_END_SIMULATION = getMessage(
            INT_MESSAGE_END_SIMULATION, IC_END_SIMULATION);
    public final MessageTemplate MESSAGE_TEMPLATE_END_SIMULATION = getMessageTemplate(
            INT_MESSAGE_END_SIMULATION, IC_END_SIMULATION);

    // Método para obtener un mensaje con un performative y conversation ID
    // específicos
    private ACLMessage getMessage(int performative, String conversationId) {
        ACLMessage msg = new ACLMessage(performative);
        msg.setConversationId(conversationId);
        return msg;
    }

    // Método para obtener un MessageTemplate con un performative y conversation ID
    // específicos
    private MessageTemplate getMessageTemplate(int performative, String conversationId) {
        MessageTemplate mp = MessageTemplate.MatchPerformative(performative);
        MessageTemplate mi = MessageTemplate.MatchConversationId(conversationId);
        return MessageTemplate.and(mp, mi);
    }

    public void waitTime() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Agent getRegisteredAdministrador() {
        return LIST_REGISTERED_ADMINISTRATOR.get(0);
    }

    public String getClaseAgente(Agent agente) {
        Pattern regexPattern = Pattern.compile("modelsAgents\\.(\\w+)");
        Matcher matcher = regexPattern.matcher(agente.getClass().getName());
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException("Class not found");
        }
    }

    public static synchronized DFHelper getInstance() {
        if (instance == null) {
            instance = new DFHelper();
        }
        return instance;
    }

    public void registrarServicio(Agent agente) {
        if (Objects.equals(this.getClaseAgente(agente), ContainerAgentConfig.ADMINISTRATOR_CONFIG.getClassName())) {
            LIST_REGISTERED_ADMINISTRATOR.add((Administrator) agente);
        } else if (Objects.equals(this.getClaseAgente(agente), ContainerAgentConfig.TRUCK_CONFIG.getClassName())) {
            LIST_REGISTERED_TRUCK.add((Truck) agente);
        } else if (Objects.equals(this.getClaseAgente(agente), ContainerAgentConfig.TRANSPORTER_CONFIG.getClassName())) {
            LIST_REGISTERED_TRANSPORTER.add((Transporter) agente);
        } else if (Objects.equals(this.getClaseAgente(agente), ContainerAgentConfig.DISTRIBUTION_AREA_CONFIG.getClassName())) {
            LIST_REGISTERED_DISTRIBUTION_AREA.add((DistributionArea) agente);
        } else if (Objects.equals(this.getClaseAgente(agente), ContainerAgentConfig.DONOR_CONFIG.getClassName())) {
            LIST_REGISTERED_DONOR.add((Donor) agente);
        } else if (Objects.equals(this.getClaseAgente(agente), ContainerAgentConfig.COLLECTION_PLACE_CONFIG.getClassName())) {
            LIST_REGISTERED_COLLECTION_PLACE.add((CollectionPlace) agente);
        }
    }

    public ArrayList<Agent> getAgentsList(CreationAgentConfig creationAgentConfig) {
        ArrayList<Agent> listAgents = new ArrayList<>();
        if (Objects.equals(creationAgentConfig.getClassName(), ContainerAgentConfig.TRUCK_CONFIG.getClassName())) {
            listAgents.addAll(LIST_REGISTERED_TRUCK);
        } else if (Objects.equals(creationAgentConfig.getClassName(), ContainerAgentConfig.TRANSPORTER_CONFIG.getClassName())) {
            listAgents.addAll(LIST_REGISTERED_TRANSPORTER);
        } else if (Objects.equals(creationAgentConfig.getClassName(),
                ContainerAgentConfig.DISTRIBUTION_AREA_CONFIG.getClassName())) {
            listAgents.addAll(LIST_REGISTERED_DISTRIBUTION_AREA);
        } else if (Objects.equals(creationAgentConfig.getClassName(), ContainerAgentConfig.DONOR_CONFIG.getClassName())) {
            listAgents.addAll(LIST_REGISTERED_DONOR);
        } else if (Objects.equals(creationAgentConfig.getClassName(),
                ContainerAgentConfig.COLLECTION_PLACE_CONFIG.getClassName())) {
            listAgents.addAll(LIST_REGISTERED_COLLECTION_PLACE);
        }
        return listAgents;
    }

    public ArrayList<Agent> getAgentsList() {
        ArrayList<Agent> listAgents = new ArrayList<>();
        listAgents.addAll(LIST_REGISTERED_TRUCK);
        listAgents.addAll(LIST_REGISTERED_TRANSPORTER);
        listAgents.addAll(LIST_REGISTERED_DISTRIBUTION_AREA);
        listAgents.addAll(LIST_REGISTERED_DONOR);
        listAgents.addAll(LIST_REGISTERED_COLLECTION_PLACE);
        return listAgents;
    }

    public Agent getAgent(String name) {
        ArrayList<Agent> listaAgentes = new ArrayList<>();
        listaAgentes.addAll(getAgentsList(ContainerAgentConfig.TRUCK_CONFIG));
        listaAgentes.addAll(getAgentsList(ContainerAgentConfig.TRANSPORTER_CONFIG));
        listaAgentes.addAll(getAgentsList(ContainerAgentConfig.DISTRIBUTION_AREA_CONFIG));
        listaAgentes.addAll(getAgentsList(ContainerAgentConfig.DONOR_CONFIG));
        listaAgentes.addAll(getAgentsList(ContainerAgentConfig.COLLECTION_PLACE_CONFIG));

        Agent targetAgent = listaAgentes.stream()
                .filter(agent -> agent.getLocalName().equals(name))
                .findFirst()
                .orElse(null);
        return Objects.nonNull(targetAgent) ? targetAgent : null;
        // if (Objects.isNull(targetAgent)){
        // targetAgent = getAgentsList(ContainerAgentConfig.TRANSPORTER_CONFIG).stream()
        // .filter(agent -> agent.getLocalName().equals(name))
        // .findFirst()
        // .orElse(null);
        // }
        // targetAgent = getAgentsList(ContainerAgentConfig.TRANSPORTER_CONFIG).stream()
        // .filter(agent -> agent.getLocalName().equals(name))
        // .findFirst()
        // .orElse(null);
        // if (Objects.nonNull(targetAgent)) {
        // return targetAgent.getAID();
        // }
        // return null;
    }

    public ArrayList<Administrator> getLIST_REGISTERED_ADMINISTRATOR() {
        return LIST_REGISTERED_ADMINISTRATOR;
    }

    public ArrayList<Truck> getLIST_REGISTERED_TRUCK() {
        return LIST_REGISTERED_TRUCK;
    }

    public ArrayList<Transporter> getLIST_REGISTERED_TRANSPORTER() {
        return LIST_REGISTERED_TRANSPORTER;
    }

    public ArrayList<DistributionArea> getLIST_REGISTERED_DISTRIBUTION_AREA() {
        return LIST_REGISTERED_DISTRIBUTION_AREA;
    }

    public ArrayList<Donor> getLIST_REGISTERED_DONOR() {
        return LIST_REGISTERED_DONOR;
    }

    public ArrayList<CollectionPlace> getLIST_REGISTERED_COLLECTION_PLACE() {
        return LIST_REGISTERED_COLLECTION_PLACE;
    }

    public void BI_CreacionFinalizada(Agent agente) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setConversationId(IC_FINISHED_CREATION);
        msg.addReceiver(this.getRegisteredAdministrador().getAID());
        msg.setContent(this.getClaseAgente(agente));
        agente.addBehaviour(new AchieveREInitiator(agente, msg) {
            @Override
            protected void handleAgree(ACLMessage agree) {
                onEnd();
            }

            @Override
            public int onEnd() {
                System.out.println("Agente " + agente.getLocalName() + ": se registra su creación.");
                return super.onEnd(); // To change body of generated methods, choose Tools | Templates.
            }
        });
    }

    public void BR_ReInitializeData(Agent agente) {
        MessageTemplate template = MESSAGE_TEMPLATE_REINITIALIZE_DATA;
        agente.addBehaviour(new SimpleConversationResponder(this, template) {
            @Override
            protected ACLMessage handleAclMessage(ACLMessage msg) {
                println(agente, msg);
                if (agente instanceof CommonAgent) {
                    ((CommonAgent) agente).cargarInformacionAgente();
                }
                // if (Objects.equals(agente.getClass().getName(),
                //         ContainerAgentConfig.ADMINISTRATOR_CONFIG.getClassRoute())) {
                // } else if (Objects.equals(agente.getClass().getName(),
                //         ContainerAgentConfig.TRUCK_CONFIG.getClassRoute())) {
                //     Truck agenteParse = (Truck) agente;
                //     agenteParse.cargarInformacionAgente();
                // } else if (Objects.equals(agente.getClass().getName(),
                //         ContainerAgentConfig.TRANSPORTER_CONFIG.getClassRoute())) {
                //     Transporter agenteParse = (Transporter) agente;
                //     agenteParse.cargarInformacionAgente();
                // } else if (Objects.equals(agente.getClass().getName(),
                //         ContainerAgentConfig.DISTRIBUTION_AREA_CONFIG.getClassRoute())) {
                //     DistributionArea agenteParse = (DistributionArea) agente;
                //     agenteParse.cargarInformacionAgente();
                // } else if (Objects.equals(agente.getClass().getName(),
                //         ContainerAgentConfig.DONOR_CONFIG.getClassRoute())) {
                //     Donor agenteParse = (Donor) agente;
                //     agenteParse.cargarInformacionAgente();
                // } else if (Objects.equals(agente.getClass().getName(),
                //         ContainerAgentConfig.COLLECTION_PLACE_CONFIG.getClassRoute())) {
                //     CollectionPlace agenteParse = (CollectionPlace) agente;
                //     agenteParse.cargarInformacionAgente();
                // }
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                return reply;
            }
        });
    }

    public void BR_UpdateAgentState(Agent agente) {
        MessageTemplate template = MESSAGE_TEMPLATE_UPDATE_AGENT_STATE;
        agente.addBehaviour(new AchieveREResponder(this, template) {
            protected ACLMessage prepareResponse(ACLMessage request) {
                ACLMessage reply = request.createReply();
                if (Objects.equals(agente.getClass().getName(),
                        ContainerAgentConfig.ADMINISTRATOR_CONFIG.getClassRoute())) {
                } else if (Objects.equals(agente.getClass().getName(),
                        ContainerAgentConfig.TRUCK_CONFIG.getClassRoute())) {
                    Truck agenteParse = (Truck) agente;
                    agenteParse.setEnabled(Boolean.parseBoolean(request.getContent()));
                } else if (Objects.equals(agente.getClass().getName(),
                        ContainerAgentConfig.TRANSPORTER_CONFIG.getClassRoute())) {
                    Transporter agenteParse = (Transporter) agente;
                    agenteParse.setEnabled(Boolean.parseBoolean(request.getContent()));
                } else if (Objects.equals(agente.getClass().getName(),
                        ContainerAgentConfig.DISTRIBUTION_AREA_CONFIG.getClassRoute())) {
                    DistributionArea agenteParse = (DistributionArea) agente;
                    agenteParse.setEnabled(Boolean.parseBoolean(request.getContent()));
                } else if (Objects.equals(agente.getClass().getName(),
                        ContainerAgentConfig.DONOR_CONFIG.getClassRoute())) {
                    Donor agenteParse = (Donor) agente;
                    agenteParse.setEnabled(Boolean.parseBoolean(request.getContent()));
                } else if (Objects.equals(agente.getClass().getName(),
                        ContainerAgentConfig.COLLECTION_PLACE_CONFIG.getClassRoute())) {
                    CollectionPlace agenteParse = (CollectionPlace) agente;
                    agenteParse.setEnabled(Boolean.parseBoolean(request.getContent()));
                }
                reply.setPerformative(ACLMessage.INFORM);
                return reply;
            }
        });
    }

    public void addAllReceiver(ACLMessage msg, ArrayList<Agent> agents) {
        msg.clearAllReceiver();
        for (Agent agent : agents) {
            msg.addReceiver(agent.getAID());
        }
    }

    public ArrayList<Administrator> getListRegisteredAdministrator() {
        return LIST_REGISTERED_ADMINISTRATOR;
    }

    public ArrayList<Administrator> getListRegisteredAdministratorEnabled() {
        return (ArrayList<Administrator>) this.getListRegisteredAdministrator().stream()
                // .filter(agent -> agent.getEnabled())
                .collect(Collectors.toList());
    }

    public ArrayList<Truck> getListRegisteredTruck() {
        return LIST_REGISTERED_TRUCK;
    }

    public ArrayList<Truck> getListRegisteredTruckEnabled() {
        return (ArrayList<Truck>) this.getListRegisteredTruck().stream()
                // .filter(agent -> agent.getEnabled())
                .collect(Collectors.toList());
    }

    public ArrayList<Transporter> getListRegisteredTransporter() {
        return LIST_REGISTERED_TRANSPORTER;
    }

    public ArrayList<Transporter> getListRegisteredTransporterEnabled() {
        return (ArrayList<Transporter>) this.getListRegisteredTransporter().stream()
                .filter(agent -> agent.getEnabled())
                .collect(Collectors.toList());
    }

    public ArrayList<DistributionArea> getListRegisteredDistributionArea() {
        return LIST_REGISTERED_DISTRIBUTION_AREA;
    }

    public ArrayList<DistributionArea> getListRegisteredDistributionAreaEnabled() {
        return (ArrayList<DistributionArea>) this.getListRegisteredDistributionArea().stream()
                // .filter(agent -> agent.getEnabled())
                .collect(Collectors.toList());
    }

    public ArrayList<Donor> getListRegisteredDonor() {
        return LIST_REGISTERED_DONOR;
    }

    public ArrayList<Donor> getListRegisteredDonorEnabled() {
        return (ArrayList<Donor>) this.getListRegisteredDonor().stream()
                // .filter(agent -> agent.getEnabled())
                .collect(Collectors.toList());
    }

    public ArrayList<CollectionPlace> getListRegisteredCollectionPlace() {
        return LIST_REGISTERED_COLLECTION_PLACE;
    }

    public ArrayList<CollectionPlace> getListRegisteredCollectionPlaceEnabled() {
        System.out.println();
        return (ArrayList<CollectionPlace>) this.getListRegisteredCollectionPlace().stream()
                .filter(agent -> agent.getEnabled())
                .collect(Collectors.toList());
    }

    public void println(Agent agente, ACLMessage message) {
        String performativeString;
        switch (message.getPerformative()) {
            case ACLMessage.ACCEPT_PROPOSAL:
                performativeString = "ACCEPT_PROPOSAL";
                break;
            case ACLMessage.REQUEST:
                performativeString = "REQUEST";
                break;
            case ACLMessage.CFP:
                performativeString = "CFP";
                break;
            case ACLMessage.INFORM:
                performativeString = "INFORM";
                break;
            case ACLMessage.REJECT_PROPOSAL:
                performativeString = "REJECT_PROPOSAL";
                break;
            case ACLMessage.AGREE:
                performativeString = "AGREE";
                break;
            // Add other cases for different performative types...
            default:
                performativeString = "UNKNOWN";
                break;
        }
        System.out.println(agente.getLocalName() + ": " + performativeString + " message received from "
                + message.getSender().getLocalName() + " with conversationId " + message.getConversationId());
    }

    public void println(Agent agente, String message) {
        System.out.println(agente.getLocalName() + ": " + message);
    }

    public void println(String message) {
        System.out.println("----- " + message + " -----");
    }

}



