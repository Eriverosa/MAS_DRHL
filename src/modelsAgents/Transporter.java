package src.modelsAgents;

import java.util.ArrayList;
import java.util.Arrays;
import com.google.gson.Gson;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;

public class Transporter extends Agent implements CommonAgent{
    private final DFHelper DF_HELPER = DFHelper.getInstance();
    private Boolean enabled;
    private ArrayList<String> truckPayroll;
    private ArrayList<Object> listaArgumentos;

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

    public ArrayList<String> getTruckPayroll() {
        return truckPayroll;
    }

    public void setTruckPayroll(ArrayList<String> truckPayroll) {
        this.truckPayroll = truckPayroll;
    }

    @Override
    protected void setup() {
        listaArgumentos = new ArrayList<>(Arrays.asList(getArguments()));
        this.cargarInformacionAgente();
        // RESPONDERS
        this.BR_ConsultTransporter();
        this.BR_RegisterCarrier();
        DF_HELPER.BR_UpdateAgentState(this);
        DF_HELPER.BR_ReInitializeData(this);
        DF_HELPER.registrarServicio(this);
        // INITIATORS
        DF_HELPER.BI_CreacionFinalizada(this);
    }

    public void cargarInformacionAgente() {
        this.enabled = true;
        this.truckPayroll = new ArrayList<>();
    }

    public void BR_RegisterCarrier() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId(DF_HELPER.IC_REGISTER_TO_CARRIER));
        addBehaviour(new AchieveREResponder(this, template) {
            private ArrayList<String> truckPayroll;

            // @Override
            // public void onStart() {
            // this.truckPayroll = getTruckPayroll();
            // super.onStart();
            // }

            public void getValues() {
                this.truckPayroll = getTruckPayroll();
                // this.enabled = getEnabled();
                // this.materialStock = getMaterialStock();
            }

            @Override
            protected ACLMessage handleRequest(ACLMessage request) {
                getValues();
                ACLMessage reply = request.createReply();
                System.out.println("Mensaje recibido de " + request.getSender().getLocalName());
                Integer tipo = ACLMessage.AGREE;
                reply.setPerformative(tipo);
                return reply;
            }

            @Override
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
                this.truckPayroll.add(request.getSender().getLocalName());
                System.out.println("Agente camion registrado");
                return null;
            }

        });
    }

    public void BR_ConsultTransporter() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP),
                MessageTemplate.MatchConversationId(DF_HELPER.IC_CONSULT_TRANSPORTER));
        this.addBehaviour(new ContractNetResponder(this, template) {
            private ArrayList<String> truckPayroll;

            @Override
            public void onStart() {
                this.truckPayroll = getTruckPayroll();
                super.onStart();
            }

            protected ACLMessage handleCfp(ACLMessage cfp) {
                DF_HELPER.println(this.getAgent(), cfp);
                ACLMessage reply = cfp.createReply();
                reply.setPerformative(getEnabled() ? ACLMessage.PROPOSE : ACLMessage.REFUSE);
                reply.setContent(new Gson().toJson(this.truckPayroll));
                return reply;
            }

            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
                DF_HELPER.println(this.getAgent(), cfp);
                ACLMessage reply = accept.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent("Proposal accepted!");
                return reply;
            }

            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                DF_HELPER.println(this.getAgent(), reject);
            }
        });
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
