package src.modelsAgents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Vector;

import com.google.gson.Gson;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetInitiator;
import jade.proto.ContractNetResponder;
import jade.proto.ProposeResponder;
import jade.proto.SimpleAchieveREResponder;
import jade.proto.SubscriptionResponder;
import jade.proto.TwoPhResponder;
// import src.commons.AgentConfig;
import src.models.MaterialStock;
import src.models.SupplyActivityProposed;
import src.models.SupplyActivityTransportation;
import src.models.SupplyActivityRequired;
import src.models.SupplyActivity;
import src.models.Ubication;
import src.commons.AgentConfigOld;
import src.behaviours.SimpleResponder;
import src.commons.AgentConfig;
import src.commons.ParametersConfig;

public class CollectionPlace extends Agent {
    private final DFHelper DF_HELPER = DFHelper.getInstance();
    private Boolean enabled;
    private MaterialStock materialStock;
    private Integer poblacion;
    private Ubication ubication;
    private SupplyActivity supplyActivity;
    private ArrayList<SupplyActivity> pendingSupplyActivityList;
    private ArrayList<SupplyActivity> supplyActivitiesList;
    private ArrayList<Integer> listTest;
    private AgentConfig agentConfig;

    private long initTime;

    @Override
    protected void setup() {
        // listTest = new ArrayList<>();
        // listTest.add(1);
        agentConfig = new AgentConfig();
        pendingSupplyActivityList = new ArrayList<>();
        supplyActivitiesList = new ArrayList<>();
        ArrayList<Object> listaArgumentos = new ArrayList<>(Arrays.asList(getArguments()));
        cargarInformacionAgente(listaArgumentos);
        // RESPONDERS
        DF_HELPER.BR_UpdateAgentState(this);
        BR_RequestInitializatorSimulation();
        BR_InitializeSimulation();
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

    public void BR_RequestInitializatorSimulation() {
        // doWait(1000);
        // DF_HELPER.waitTime();
        MessageTemplate template = DF_HELPER.MESSAGE_TEMPLATE_REQUEST_INITIALIZATOR_SIMULATION;
        addBehaviour(new ContractNetResponder(this, template) {
            protected ACLMessage handleCfp(ACLMessage cfp) {
                DF_HELPER.println(myAgent, cfp);
                ACLMessage inform = cfp.createReply();
                inform.setPerformative(ACLMessage.PROPOSE);
                inform.setContent("1");
                return inform;
            }


            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
                DF_HELPER.println(myAgent, accept);
                ACLMessage reply = accept.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                return reply;
            }

            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                DF_HELPER.println(myAgent, reject);
            }
        });
    }

    public void BR_InitializeSimulation() {
        MessageTemplate template = DF_HELPER.MESSAGE_TEMPLATE_INITIALIZE_SIMULATION;
        this.addBehaviour(new SimpleResponder(this, template) {
            @Override
            protected void handleAclMessage(ACLMessage msg) {
                DF_HELPER.println(myAgent, msg);
                BI_ConsultRequiredSupply();
            }
        });
    }

    public void BI_ConsultRequiredSupply() {
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId(DF_HELPER.IC_CONSULT_REQUIRED_SUPPLY);
        for (Agent agent : DF_HELPER.getAgentsList(agentConfig.DISTRIBUTION_AREA_CONFIG)) {
            msg.addReceiver(agent.getAID());
        }
        msg.setContent(String.valueOf(this.initTime));
        this.addBehaviour(new ContractNetInitiator(this, msg) {
            protected ArrayList<SupplyActivity> pendingSupplyActivityList;

            public void getValues() {
                pendingSupplyActivityList = getPendingSupplyActivityList();
            }

            @Override
            protected void handleAllResponses(Vector responses, Vector acceptances) {
                getValues();
                ArrayList<ACLMessage> responsesList = new ArrayList<ACLMessage>(responses);
                responsesList.removeIf(message -> message.getPerformative() != ACLMessage.PROPOSE);
                if (!responsesList.isEmpty()) {
                    orderRequireSupplyObj(responsesList);
                    for (ACLMessage aclMessage : responsesList) {
                        ACLMessage reply = aclMessage.createReply();
                        SupplyActivityRequired supplyActivityRequired = new Gson().fromJson(aclMessage.getContent(),
                                SupplyActivityRequired.class);
                        DF_HELPER.println(myAgent, Boolean.toString(supplyActivityRequired.getMaterialStock()
                                .getNeedHelp(supplyActivityRequired.getCantidadPersonas())));
                        DF_HELPER.println(myAgent, supplyActivityRequired.getMaterialStock().toString());
                        System.out.println(supplyActivityRequired.getMaterialStock()
                                .getNeedHelp(supplyActivityRequired.getCantidadPersonas()));
                        SupplyActivity supplyActivity = new SupplyActivity();
                        supplyActivity.setSupplyActivityRequired(
                                new Gson().fromJson(aclMessage.getContent(), SupplyActivityRequired.class));
                        this.pendingSupplyActivityList.add(supplyActivity);
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        // if (supplyActivityRequired.getMaterialStock()
                        // .getNeedHelp(supplyActivityRequired.getCantidadPersonas())) {
                        // SupplyActivity supplyActivity = new SupplyActivity();
                        // supplyActivity.setSupplyActivityRequired(
                        // new Gson().fromJson(aclMessage.getContent(), SupplyActivityRequired.class));
                        // this.pendingSupplyActivityList.add(supplyActivity);
                        // reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        // } else {
                        // reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        // }
                        acceptances.addElement(reply);
                    }
                } else {
                    DF_HELPER.println(myAgent,
                            "No hay " + AgentConfigOld.DISTRIBUTION_AREA_CONFIG.getClassName()
                                    + " que puedan realizar el trabajo");
                }

            }

            protected void handleInform(ACLMessage inform) {
                DF_HELPER.println(this.getAgent(), inform);
            }

            @Override
            public int onEnd() {
                super.onEnd();
                setPendingSupplyActivityList(this.pendingSupplyActivityList);
                for (SupplyActivity supply : this.pendingSupplyActivityList) {
                    System.out.println(supply.toString(true));
                }
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
        for (Agent agent : DF_HELPER.getAgentsList(agentConfig.DONOR_CONFIG)) {
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
                if (!responsesList.isEmpty()) {
                    orderProposedSupplyObj(responsesList);
                    for (ACLMessage aclMessage : responsesList) {
                        ACLMessage reply = aclMessage.createReply();
                        if (responsesList.indexOf(aclMessage) == 0) {
                            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                            SupplyActivityProposed proposedSupply = new Gson().fromJson(aclMessage.getContent(),
                                    SupplyActivityProposed.class);
                            this.supplyActivity.setSupplyActivityProposed(proposedSupply);
                        } else {
                            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        }
                        acceptances.addElement(reply);
                    }
                } else {
                    DF_HELPER.println(myAgent,
                            "No hay " + AgentConfigOld.DONOR_CONFIG.getClassName() + " que puedan realizar el trabajo");
                }
            }

            protected void handleInform(ACLMessage inform) {
                DF_HELPER.println(this.getAgent(), inform);
            }

            @Override
            public int onEnd() {
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
        for (Agent agent : DF_HELPER.getAgentsList(agentConfig.TRANSPORTER_CONFIG)) {
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
                if (!responsesList.isEmpty()) {
                    for (ACLMessage aclMessage : responsesList) {
                        ACLMessage reply = aclMessage.createReply();
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        this.truckNameList.addAll(new Gson().fromJson(aclMessage.getContent(), ArrayList.class));
                        acceptances.addElement(reply);
                    }
                } else {
                    DF_HELPER.println(myAgent,
                            "No hay " + AgentConfigOld.TRANSPORTER_CONFIG.getClassName()
                                    + " que puedan realizar el trabajo");
                }

            }

            protected void handleInform(ACLMessage inform) {
                DF_HELPER.println(this.getAgent(), inform);
            }

            @Override
            public int onEnd() {
                super.onEnd();
                if (!this.truckNameList.isEmpty()) {
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
                if (!responsesList.isEmpty()) {
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
                } else {
                    DF_HELPER.println(myAgent,
                            "No hay " + AgentConfigOld.TRUCK_CONFIG.getClassName() + " que puedan realizar el trabajo");
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
        System.out.println(this.supplyActivity);
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
        ACLMessage msg = DF_HELPER.ACL_MESSAGE_END_SIMULATION;
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

    public ArrayList<SupplyActivity> getSupplyActivitiesList() {
        return supplyActivitiesList;
    }

    public void setSupplyActivitiesList(ArrayList<SupplyActivity> supplyActivitiesList) {
        this.supplyActivitiesList = supplyActivitiesList;
    }

    public ArrayList<Integer> getListTest() {
        return listTest;
    }

    public void setListTest(ArrayList<Integer> listTest) {
        this.listTest = listTest;
    }
}
