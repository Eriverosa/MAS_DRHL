package src.modelsAgents;

import java.rmi.StubNotFoundException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;
// import jade.util.leap.ArrayList;
import src.commons.AgentConfig;
import src.commons.AgentConfig.CreationAgentConfig;

public class DFHelper extends Agent {
    private static DFHelper instance = null;

    // public final ArrayList<Agent> LIST_AGENTS = new ArrayList<>();
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
    public final String IC_UPDATE_AGENT_STATE = "ID_CONVERSATION_UPDATE_AGENT_STATE";
    public final String IC_INITIALIZE_SIMULATION = "ID_CONVERSATION_INITIALIZE_SIMULATION";
    public final String IC_END_SIMULATION = "ID_CONVERSATION_END_SIMULATION";
    public final String IC_CONSULT_REQUIRED_SUPPLY = "ID_CONVERSATION_CONSULT_REQUIRED_SUPPLY";
    public final String IC_CONSULT_PROPOSED_SUPPLY = "ID_CONVERSATION_CONSULT_PROPOSED_SUPPLY";
    public final String IC_CONSULT_TRANSPORTER = "ID_CONVERSATION_CONSULT_TRANSPORT";
    public final String IC_REQUEST_FREIGHT = "ID_CONVERSATION_CONSULT_FREIGHT";
    public final String IC_REGISTER_TO_CARRIER = "ID_CONVERSATION_REGISTER_TO_CARRIER";
    public final String IC_CONFIRM_SUPPLY_ACTIVITY = "ID_CONVERSATION_CONFIRM_SUPPLY_ACTIVITY";

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
        if (Objects.equals(this.getClaseAgente(agente), AgentConfig.ADMINISTRATOR_CONFIG.getClassName())) {
            LIST_REGISTERED_ADMINISTRATOR.add((Administrator) agente);
        } else if (Objects.equals(this.getClaseAgente(agente), AgentConfig.TRUCK_CONFIG.getClassName())) {
            LIST_REGISTERED_TRUCK.add((Truck) agente);
        } else if (Objects.equals(this.getClaseAgente(agente), AgentConfig.TRANSPORTER_CONFIG.getClassName())) {
            LIST_REGISTERED_TRANSPORTER.add((Transporter) agente);
        } else if (Objects.equals(this.getClaseAgente(agente), AgentConfig.DISTRIBUTION_AREA_CONFIG.getClassName())) {
            LIST_REGISTERED_DISTRIBUTION_AREA.add((DistributionArea) agente);
        } else if (Objects.equals(this.getClaseAgente(agente), AgentConfig.DONOR_CONFIG.getClassName())) {
            LIST_REGISTERED_DONOR.add((Donor) agente);
        } else if (Objects.equals(this.getClaseAgente(agente), AgentConfig.COLLECTION_PLACE_CONFIG.getClassName())) {
            LIST_REGISTERED_COLLECTION_PLACE.add((CollectionPlace) agente);
        }
    }

    public ArrayList<Agent> getAgentsList(CreationAgentConfig creationAgentConfig) {
        ArrayList<Agent> listAgents = new ArrayList<>();
        if (Objects.equals(creationAgentConfig.getClassName(), AgentConfig.TRUCK_CONFIG.getClassName())) {
            listAgents.addAll(LIST_REGISTERED_TRUCK);
        } else if (Objects.equals(creationAgentConfig.getClassName(), AgentConfig.TRANSPORTER_CONFIG.getClassName())) {
            listAgents.addAll(LIST_REGISTERED_TRANSPORTER);
        } else if (Objects.equals(creationAgentConfig.getClassName(),
                AgentConfig.DISTRIBUTION_AREA_CONFIG.getClassName())) {
            listAgents.addAll(LIST_REGISTERED_DISTRIBUTION_AREA);
        } else if (Objects.equals(creationAgentConfig.getClassName(), AgentConfig.DONOR_CONFIG.getClassName())) {
            listAgents.addAll(LIST_REGISTERED_DONOR);
        } else if (Objects.equals(creationAgentConfig.getClassName(),
                AgentConfig.COLLECTION_PLACE_CONFIG.getClassName())) {
            listAgents.addAll(LIST_REGISTERED_COLLECTION_PLACE);
        }
        return listAgents;
    }

    public Agent getAgent(String name) {
        ArrayList<Agent> listaAgentes = new ArrayList<>();
        listaAgentes.addAll(getAgentsList(AgentConfig.TRUCK_CONFIG));
        listaAgentes.addAll(getAgentsList(AgentConfig.TRANSPORTER_CONFIG));
        listaAgentes.addAll(getAgentsList(AgentConfig.DISTRIBUTION_AREA_CONFIG));
        listaAgentes.addAll(getAgentsList(AgentConfig.DONOR_CONFIG));
        listaAgentes.addAll(getAgentsList(AgentConfig.COLLECTION_PLACE_CONFIG));

        Agent targetAgent = listaAgentes.stream()
                .filter(agent -> agent.getLocalName().equals(name))
                .findFirst()
                .orElse(null);
        return Objects.nonNull(targetAgent) ? targetAgent : null;
        // if (Objects.isNull(targetAgent)){
        // targetAgent = getAgentsList(AgentConfig.TRANSPORTER_CONFIG).stream()
        // .filter(agent -> agent.getLocalName().equals(name))
        // .findFirst()
        // .orElse(null);
        // }
        // targetAgent = getAgentsList(AgentConfig.TRANSPORTER_CONFIG).stream()
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

    public void BR_UpdateAgentState(Agent agente) {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId(IC_UPDATE_AGENT_STATE));

        agente.addBehaviour(new AchieveREResponder(agente, template) {
            protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
                Boolean enable = Boolean.parseBoolean(request.getContent());
                if (Objects.equals(agente.getClass().getName(),
                        AgentConfig.ADMINISTRATOR_CONFIG.getClassRoute())) {
                    // LIST_REGISTERED_ADMINISTRATOR.add((Administrator) agente);
                } else if (Objects.equals(agente.getClass().getName(),
                        AgentConfig.TRUCK_CONFIG.getClassRoute())) {
                    Truck agenteParse = (Truck) agente;
                    agenteParse.setEnabled(enable);
                } else if (Objects.equals(agente.getClass().getName(),
                        AgentConfig.TRANSPORTER_CONFIG.getClassRoute())) {
                    Transporter agenteParse = (Transporter) agente;
                    agenteParse.setEnabled(enable);
                } else if (Objects.equals(agente.getClass().getName(),
                        AgentConfig.DISTRIBUTION_AREA_CONFIG.getClassRoute())) {
                    DistributionArea agenteParse = (DistributionArea) agente;
                    agenteParse.setEnabled(enable);
                } else if (Objects.equals(agente.getClass().getName(),
                        AgentConfig.DONOR_CONFIG.getClassRoute())) {
                    Donor agenteParse = (Donor) agente;
                    agenteParse.setEnabled(enable);
                } else if (Objects.equals(agente.getClass().getName(),
                        AgentConfig.COLLECTION_PLACE_CONFIG.getClassRoute())) {
                    CollectionPlace agenteParse = (CollectionPlace) agente;
                    agenteParse.setEnabled(enable);
                }
                return request.createReply();
            }
        });

        // agente.addBehaviour(new ContractNetResponder(agente, template) {
        // protected ACLMessage handleCfp(ACLMessage cfp) {
        // // Recibir y procesar el mensaje de tipo INFORM
        // System.out.println("Mensaje INFORM recibido del agente " +
        // cfp.getSender().getLocalName());
        // System.out.println("Contenido del mensaje: " + cfp.getContent());
        // // Responder con un mensaje de tipo INFORM
        // ACLMessage reply = cfp.createReply();
        // reply.setPerformative(ACLMessage.INFORM);
        // reply.setContent("Respuesta INFORM del agente responder");
        // return reply;
        // }
        // // protected ACLMessage handleRequest(ACLMessage request) {
        // // System.out.println("Llegó al responder");
        // // if (Objects.equals(agente.getClass().getName(),
        // // AgentConfig.ADMINISTRATOR_CONFIG.getClassRoute())) {
        // // // LIST_REGISTERED_ADMINISTRATOR.add((Administrator) agente);
        // // } else if (Objects.equals(agente.getClass().getName(),
        // // AgentConfig.TRUCK_CONFIG.getClassRoute())) {
        // // Truck agenteParse = (Truck) agente;
        // // agenteParse.setEnabled(Boolean.parseBoolean(request.getContent()));
        // // } else if (Objects.equals(agente.getClass().getName(),
        // // AgentConfig.TRANSPORTER_CONFIG.getClassRoute())) {
        // // Transporter agenteParse = (Transporter) agente;
        // // agenteParse.setEnabled(Boolean.parseBoolean(request.getContent()));
        // // } else if (Objects.equals(agente.getClass().getName(),
        // // AgentConfig.DISTRIBUTION_AREA_CONFIG.getClassRoute())) {
        // // DistributionArea agenteParse = (DistributionArea) agente;
        // // agenteParse.setEnabled(Boolean.parseBoolean(request.getContent()));
        // // } else if (Objects.equals(agente.getClass().getName(),
        // // AgentConfig.DONOR_CONFIG.getClassRoute())) {
        // // Donor agenteParse = (Donor) agente;
        // // agenteParse.setEnabled(Boolean.parseBoolean(request.getContent()));
        // // } else if (Objects.equals(agente.getClass().getName(),
        // // AgentConfig.COLLECTION_PLACE_CONFIG.getClassRoute())) {
        // // CollectionPlace agenteParse = (CollectionPlace) agente;
        // // agenteParse.setEnabled(Boolean.parseBoolean(request.getContent()));
        // // }
        // // ACLMessage response = request.createReply();
        // // response.setPerformative(ACLMessage.INFORM);
        // // return response;
        // // }

        // // @Override
        // // protected ACLMessage prepareResultNotification(ACLMessage request,
        // ACLMessage
        // // response) {
        // // return null;
        // // }
        // });
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
