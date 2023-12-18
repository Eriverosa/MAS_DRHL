package src.modelsAgents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Vector;

import org.apache.commons.lang3.ObjectUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetInitiator;
import src.commons.AgentConfig;
import src.commons.FileGenerator;
import src.models.MaterialStock;
import src.models.SupplyActivityProposed;
import src.models.SupplyActivityTransportation;
import src.models.SupplyActivityRequired;
import src.models.SupplyActivity;
import src.models.Ubication;
import src.commons.ParametersConfig;

public class CollectionPlace extends Agent {
    private final DFHelper DF_HELPER = DFHelper.getInstance();
    private Boolean enabled;
    private MaterialStock materialStock;
    private Integer poblacion;
    private Ubication ubication;
    private SupplyActivity supplyActivity;
    private ArrayList<SupplyActivity> pendingSupplyActivityList;
    private ArrayList<SupplyActivity> supplyActivitiesList = new ArrayList<>();
    private long initTime;
    // private RequiredSupply requiredSupply;
    // private ProposedSupply proposedSupply;

    // public RequiredSupply getRequiredSupply() {
    // return requiredSupply;
    // }

    // public void setRequiredSupply(RequiredSupply requiredSupply) {
    // this.requiredSupply = requiredSupply;
    // }

    public Ubication getUbication() {
        return ubication;
    }

    public void setUbication(Ubication ubication) {
        this.ubication = ubication;
    }

    public Integer getPoblacion() {
        return poblacion;
    }

    public void setPoblacion(Integer poblacion) {
        this.poblacion = poblacion;
    }

    public void setMaterialStock(MaterialStock materialStock) {
        this.materialStock = materialStock;
    }

    @Override
    protected void setup() {
        ArrayList<Object> listaArgumentos = new ArrayList<>(Arrays.asList(getArguments()));
        this.cargarInformacionAgente(listaArgumentos);
        // RESPONDERS
        DF_HELPER.BR_UpdateAgentState(this);
        this.BR_InitializeSimulation();
        // INITIATORS
        DF_HELPER.registrarServicio(this);
        DF_HELPER.BI_CreacionFinalizada(this);
    }

    public void cargarInformacionAgente(ArrayList<Object> listaArgumentos) {
        this.enabled = true;
        // this.setPoblacion(Integer.parseInt((String) listaArgumentos.get(1)));
        this.setUbication(new Ubication(Integer.parseInt((String) listaArgumentos.get(0)),
                Integer.parseInt((String) listaArgumentos.get(1))));
        ArrayList<Integer> listStockMaterial = new ArrayList<>(
                Arrays.asList(Integer.parseInt((String) listaArgumentos.get(7)),
                        Integer.parseInt((String) listaArgumentos.get(6)),
                        Integer.parseInt((String) listaArgumentos.get(5)),
                        Integer.parseInt((String) listaArgumentos.get(4)),
                        Integer.parseInt((String) listaArgumentos.get(3)),
                        Integer.parseInt((String) listaArgumentos.get(2))));
        this.setMaterialStock(new MaterialStock(listStockMaterial));
    }

    public void BR_InitializeSimulation() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(DF_HELPER.IC_INITIALIZE_SIMULATION));

        this.addBehaviour(new AchieveREResponder(this, template) {
            protected ACLMessage handleRequest(ACLMessage request) {
                DF_HELPER.println(this.myAgent, "Llega la solicitud para iniciar");
                ACLMessage agree = request.createReply();
                agree.setPerformative(getEnabled() ? ACLMessage.AGREE : ACLMessage.REFUSE);
                return agree;
            }

            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
                DF_HELPER.println("Inicializa simulacion");
                supplyActivity = new SupplyActivity();
                supplyActivitiesList = new ArrayList<>();
                pendingSupplyActivityList = new ArrayList<>();
                initTime = Long.parseLong(request.getContent());
                try {
                    Thread.sleep(1000);
                    BI_ConsultRequiredSupply();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

        });
    }

    public void BI_ConsultRequiredSupply() {
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId(DF_HELPER.IC_CONSULT_REQUIRED_SUPPLY);
        for (Agent agent : DF_HELPER.getAgentsList(AgentConfig.DISTRIBUTION_AREA_CONFIG)) {
            msg.addReceiver(agent.getAID());
        }
        msg.setContent(String.valueOf(this.initTime));
        this.addBehaviour(new ContractNetInitiator(this, msg) {
            ArrayList<SupplyActivity> pendingSupplyActivityList;

            @Override
            public void onStart() {
                this.pendingSupplyActivityList = getPendingSupplyActivityList();
                super.onStart();
            }

            protected void handleAllResponses(Vector responses, Vector acceptances) {
                ArrayList<ACLMessage> responsesList = new ArrayList<ACLMessage>(responses);
                responsesList.removeIf(message -> message.getPerformative() != ACLMessage.PROPOSE);
                orderRequireSupplyObj(responsesList);
                for (ACLMessage aclMessage : responsesList) {
                    ACLMessage reply = aclMessage.createReply();
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    acceptances.addElement(reply);
                    SupplyActivity supplyActivity = new SupplyActivity();
                    supplyActivity.setSupplyActivityRequired(new Gson()
                            .fromJson(aclMessage.getContent(), SupplyActivityRequired.class));
                    this.pendingSupplyActivityList.add(supplyActivity);
                }
            }

            protected void handleInform(ACLMessage inform) {
                DF_HELPER.println(this.getAgent(), inform);
            }

            @Override
            public int onEnd() {
                super.onEnd();
                F_SetActualSupplyActivity();
                return 0;
            }
        });
    }

    public void F_SetActualSupplyActivity() {
        if (this.pendingSupplyActivityList.isEmpty()) {
            BI_EndSimulation();
        } else {
            this.supplyActivity = pendingSupplyActivityList.get(0);
            pendingSupplyActivityList.remove(this.supplyActivity);
            this.BI_ConsultProposedSupply();
        }
    }

    public void BI_ConsultProposedSupply() {
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId(DF_HELPER.IC_CONSULT_PROPOSED_SUPPLY);
        msg.setContent(new Gson().toJson(supplyActivity.getSupplyActivityRequired()));
        for (Agent agent : DF_HELPER.getAgentsList(AgentConfig.DONOR_CONFIG)) {
            msg.addReceiver(agent.getAID());
        }
        this.addBehaviour(new ContractNetInitiator(this, msg) {
            SupplyActivity supplyActivity;

            @Override
            public void onStart() {
                this.supplyActivity = getSupplyActivity();
                super.onStart();
            }

            protected void handleAllResponses(Vector responses, Vector acceptances) {
                ArrayList<ACLMessage> responsesList = new ArrayList<>(responses);
                responsesList.removeIf(message -> message.getPerformative() != ACLMessage.PROPOSE);
                orderProposedSupplyObj(responsesList);
                for (ACLMessage aclMessage : responsesList) {
                    ACLMessage reply = aclMessage.createReply();
                    if (responsesList.indexOf(aclMessage) == 0) {
                        System.out.println("ACEPT");
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        SupplyActivityProposed proposedSupply = new Gson().fromJson(aclMessage.getContent(),
                                SupplyActivityProposed.class);
                        this.supplyActivity.setSupplyActivityProposed(proposedSupply);
                    } else {
                        System.out.println("REJECT");
                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    }
                    acceptances.addElement(reply);
                }
            }

            protected void handleInform(ACLMessage inform) {
                DF_HELPER.println(this.getAgent(), inform);
            }

            @Override
            public int onEnd() {
                System.out.println(this.supplyActivity.getSupplyActivityProposed());
                if (Objects.nonNull(this.supplyActivity.getSupplyActivityProposed())) {
                    BI_ConsultTransport();
                } else {
                    F_SetActualSupplyActivity();
                }
                super.onEnd();
                return 0;
            }
        });
    }

    public void BI_ConsultTransport() {
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId(DF_HELPER.IC_CONSULT_TRANSPORTER);
        for (Agent agent : DF_HELPER.getAgentsList(AgentConfig.TRANSPORTER_CONFIG)) {
            msg.addReceiver(agent.getAID());
        }
        this.addBehaviour(new ContractNetInitiator(this, msg) {
            ArrayList<String> truckNameList;

            @Override
            public void onStart() {
                this.truckNameList = new ArrayList<>();
                super.onStart();
            }

            protected void handleAllResponses(Vector responses, Vector acceptances) {
                ArrayList<ACLMessage> responsesList = new ArrayList<>(responses);
                responsesList.removeIf(message -> message.getPerformative() != ACLMessage.PROPOSE);
                for (ACLMessage aclMessage : responsesList) {
                    ACLMessage reply = aclMessage.createReply();
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    this.truckNameList.addAll(new Gson().fromJson(aclMessage.getContent(), ArrayList.class));
                    acceptances.addElement(reply);
                }
            }

            protected void handleInform(ACLMessage inform) {
                DF_HELPER.println(this.getAgent(), inform);
            }

            @Override
            public int onEnd() {
                super.onEnd();
                if (!truckNameList.isEmpty()) {
                    BI_ConsultFreight(this.truckNameList);
                } else {
                    F_SetActualSupplyActivity();
                }
                return 0;
            }
        });
    }

    public void BI_ConsultFreight(ArrayList<String> truckNameList) {
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId(DF_HELPER.IC_REQUEST_FREIGHT);
        for (String nameTruck : truckNameList) {
            msg.addReceiver(DF_HELPER.getAgent(nameTruck).getAID());
        }
        msg.setContent(new Gson().toJson(supplyActivity));
        this.addBehaviour(new ContractNetInitiator(this, msg) {
            protected SupplyActivity supplyActivity;

            @Override
            public void onStart() {
                this.supplyActivity = getSupplyActivity();
                super.onStart();
            }

            protected void handleAllResponses(Vector responses, Vector acceptances) {
                ArrayList<ACLMessage> responsesList = new ArrayList<>(responses);
                responsesList.removeIf(message -> message.getPerformative() != ACLMessage.PROPOSE);
                orderProposedTransportation(responsesList, ParametersConfig.ASC_STRING);
                for (ACLMessage aclMessage : responsesList) {
                    ACLMessage reply = aclMessage.createReply();
                    if (responsesList.indexOf(aclMessage) == 0) {
                        SupplyActivityTransportation proposedTransportation = new Gson().fromJson(
                                aclMessage.getContent(),
                                SupplyActivityTransportation.class);
                        this.supplyActivity.setSupplyActivityTransportation(proposedTransportation);
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    } else {
                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    }
                    acceptances.addElement(reply);
                }
            }

            protected void handleInform(ACLMessage inform) {
                DF_HELPER.println(this.getAgent(), inform);
            }

            @Override
            public int onEnd() {
                super.onEnd();
                BI_ConfirmSupplyActivity();
                return 0;
            }
        });
    }

    public void BI_ConfirmSupplyActivity() {
        this.supplyActivity.generateSupplyActivityOrder();
        this.supplyActivitiesList.add(this.supplyActivity);
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setConversationId(DF_HELPER.IC_CONFIRM_SUPPLY_ACTIVITY);
        msg.addReceiver(DF_HELPER.getAgent(this.supplyActivity.getSupplyActivityRequired().getAgentName()).getAID());
        msg.addReceiver(DF_HELPER.getAgent(this.supplyActivity.getSupplyActivityProposed().getAgentName()).getAID());
        msg.addReceiver(
                DF_HELPER.getAgent(this.supplyActivity.getSupplyActivityTransportation().getAgentName()).getAID());
        msg.setContent(new Gson().toJson(this.supplyActivity));
        this.addBehaviour(new AchieveREInitiator(this, msg) {
            @Override
            protected void handleAllResultNotifications(Vector notifications) {
                DF_HELPER.println(myAgent, "Llegaron todos los mensajes");
            }

            @Override
            public int onEnd() {
                super.onEnd();
                F_SetActualSupplyActivity();
                return 0;
            }
        });
    }

    public void BI_EndSimulation() {
        DF_HELPER.println("END SIMULATION");
        Agent agenteReceiver = DF_HELPER.getRegisteredAdministrador();
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setConversationId(DF_HELPER.IC_END_SIMULATION);
        msg.addReceiver(agenteReceiver.getAID());
        msg.setContent(new Gson().toJson(this.supplyActivitiesList));
        send(msg);
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled() {
        this.enabled = true;
    }

    public void setEnabled(Boolean val) {
        this.enabled = val;
    }

    public void setDisabled() {
        this.enabled = false;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public void orderRequireSupplyObj(ArrayList<ACLMessage> responsesList) {
        Collections.sort(responsesList, new Comparator<ACLMessage>() {
            @Override
            public int compare(ACLMessage aclMessage1, ACLMessage aclMessage2) {
                Gson gson = new Gson();
                SupplyActivityRequired obj1 = gson.fromJson(aclMessage1.getContent(), SupplyActivityRequired.class);
                SupplyActivityRequired obj2 = gson.fromJson(aclMessage2.getContent(), SupplyActivityRequired.class);
                // RequiredSupply obj1 = new RequiredSupply(aclMessage1.getContent());
                // RequiredSupply obj2 = new RequiredSupply(aclMessage2.getContent());
                double weigh_p_1 = ParametersConfig.REQUIRED_SUPPLY_PERSONAS_WEIGHING * obj1.getCantidadPersonas();
                double distance_1 = ParametersConfig.REQUIRED_SUPPLY_DISTANCIA_WEIGHING
                        * (Math.sqrt(Math.pow(obj1.getUbicacion().getLatitud() - getUbication().getLatitud(), 2)
                                + Math.pow(obj1.getUbicacion().getLongitud() - getUbication().getLongitud(), 2)));
                double weigh_final_1 = weigh_p_1 + distance_1;
                double weigh_p_2 = ParametersConfig.REQUIRED_SUPPLY_PERSONAS_WEIGHING * obj2.getCantidadPersonas();
                double distance_2 = ParametersConfig.REQUIRED_SUPPLY_DISTANCIA_WEIGHING
                        * (Math.sqrt(Math.pow(obj2.getUbicacion().getLatitud() - getUbication().getLatitud(), 2)
                                + Math.pow(obj2.getUbicacion().getLongitud() - getUbication().getLongitud(), 2)));
                double weigh_final_2 = weigh_p_2 + distance_2;
                return Double.compare(weigh_final_2, weigh_final_1);
            }
        });
    }

    public void orderProposedSupplyObj(ArrayList<ACLMessage> responsesList) {
        Collections.sort(responsesList, new Comparator<ACLMessage>() {
            @Override
            public int compare(ACLMessage msg1, ACLMessage msg2) {
                SupplyActivityProposed obj1 = new Gson().fromJson(msg1.getContent(), SupplyActivityProposed.class);
                SupplyActivityProposed obj2 = new Gson().fromJson(msg2.getContent(), SupplyActivityProposed.class);
                int content1 = obj1.getMaterialStock().getTotalAmountHelpByPerson();
                int content2 = obj2.getMaterialStock().getTotalAmountHelpByPerson();
                return Integer.compare(content2, content1);
            }
        });
        // No es necesario retornar nada, ya que la lista se ordena directamente
    }

    public void orderProposedTransportation(ArrayList<ACLMessage> responsesList, String type) {
        Collections.sort(responsesList, new Comparator<ACLMessage>() {
            @Override
            public int compare(ACLMessage msg1, ACLMessage msg2) {
                SupplyActivityTransportation obj1 = new Gson().fromJson(msg1.getContent(),
                        SupplyActivityTransportation.class);
                SupplyActivityTransportation obj2 = new Gson().fromJson(msg2.getContent(),
                        SupplyActivityTransportation.class);
                double value1 = obj1.getHoraInicioCarga() * ParametersConfig.PROPOSED_START_TIME_WEIGHING
                        + obj1.getHoraFinDescarga() * ParametersConfig.PROPOSED_END_TIME_WEIGHING
                        + obj1.getCantidadTrasladada() * ParametersConfig.PROPOSED_QUANTITY_TRANSPORTED_WEIGHING;
                double value2 = obj2.getHoraInicioCarga() * ParametersConfig.PROPOSED_START_TIME_WEIGHING
                        + obj2.getHoraFinDescarga() * ParametersConfig.PROPOSED_END_TIME_WEIGHING
                        + obj2.getCantidadTrasladada() * ParametersConfig.PROPOSED_QUANTITY_TRANSPORTED_WEIGHING;
                return type.equals(ParametersConfig.ASC_STRING) ? Double.compare(value1, value2)
                        : Double.compare(value2, value1);
            }
        });
    }

    public void orderConsultProposedSupplyObj(ArrayList<ACLMessage> responsesList) {
        Collections.sort(responsesList, new Comparator<ACLMessage>() {
            @Override
            public int compare(ACLMessage msg1, ACLMessage msg2) {
                // System.out.println(msg1.);
                int content1 = Integer.parseInt(msg1.getContent());
                int content2 = Integer.parseInt(msg2.getContent());
                return Integer.compare(content2, content1);
            }
        });
        // No es necesario retornar nada, ya que la lista se ordena directamente
    }

    public MaterialStock getMaterialStock() {
        return materialStock;
    }

    public DFHelper getDF_HELPER() {
        return DF_HELPER;
    }

    public SupplyActivity getSupplyActivity() {
        return supplyActivity;
    }

    public void setSupplyActivity(SupplyActivity supplyActivity) {
        this.supplyActivity = supplyActivity;
    }

    public ArrayList<SupplyActivity> getPendingSupplyActivityList() {
        return pendingSupplyActivityList;
    }

    public void setPendingSupplyActivityList(ArrayList<SupplyActivity> pedingSupplyActivityList) {
        this.pendingSupplyActivityList = pedingSupplyActivityList;
    }

    public long getInitTime() {
        return initTime;
    }

    public void setInitTime(long initTime) {
        this.initTime = initTime;
    }

}
