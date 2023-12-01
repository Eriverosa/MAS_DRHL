package src.modelsAgents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import com.google.gson.Gson;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;
import src.commons.AgentConfig;
import src.commons.AgentConfig.CreationAgentConfig;

public class Transporter extends Agent {
    private final DFHelper DF_HELPER = DFHelper.getInstance();
    private Boolean enabled;
    private ArrayList<String> truckPayroll;

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
        ArrayList<Object> listaArgumentos = new ArrayList<>(Arrays.asList(getArguments()));
        this.BR_ConsultTransporter();
        this.BR_RegisterCarrier();
        this.cargarInformacionAgente(listaArgumentos);
        DF_HELPER.BR_UpdateAgentState(this);
        DF_HELPER.registrarServicio(this);
        DF_HELPER.BI_CreacionFinalizada(this);
    }

    public void cargarInformacionAgente(ArrayList<Object> listaArgumentos) {
        this.enabled = true;
        this.truckPayroll = new ArrayList<>();
    }

    public void BR_RegisterCarrier() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId(DF_HELPER.IC_REGISTER_TO_CARRIER));
        addBehaviour(new AchieveREResponder(this, template) {

            @Override
            protected ACLMessage handleRequest(ACLMessage request) {
                ACLMessage reply = request.createReply();
                System.out.println("Mensaje recibido de " + request.getSender().getLocalName());
                Integer tipo = ACLMessage.AGREE;
                reply.setPerformative(tipo);
                return reply;
            }

            @Override
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
                getTruckPayroll().add(request.getSender().getLocalName());
                System.out.println("Agente camion registrado");
                return null;
            }

        });
    }

    public void BR_ConsultTransporter() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP),
                MessageTemplate.MatchConversationId(DF_HELPER.IC_CONSULT_TRANSPORTER));
        this.addBehaviour(new ContractNetResponder(this, template) {
            protected ACLMessage handleCfp(ACLMessage cfp) {
                DF_HELPER.println(this.getAgent(), cfp);
                ACLMessage reply = cfp.createReply();
                reply.setPerformative(getEnabled() ? ACLMessage.PROPOSE : ACLMessage.FAILURE);
                reply.setContent(new Gson().toJson(getTruckPayroll()));
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
