package src.modelsAgents;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.google.gson.Gson;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;
import src.models.MaterialStock;
import src.models.SupplyActivityProposed;
import src.models.SupplyActivityRequired;
import src.models.SupplyActivityOrder;
import src.models.SupplyActivity;
import src.models.Ubication;

public class Donor extends Agent {
    private final DFHelper DF_HELPER = DFHelper.getInstance();
    private Boolean enabled;
    private MaterialStock materialStock;
    private Ubication ubication;
    private ArrayList<SupplyActivityOrder> listSupplyActivities = new ArrayList<>();

    @Override
    protected void setup() {
        ArrayList<Object> listaArgumentos = new ArrayList<>(Arrays.asList(getArguments()));
        this.BR_ConsultProposedSupply();
        this.BR_ConfirmSupplyActivity();
        this.cargarInformacionAgente(listaArgumentos);
        DF_HELPER.BR_UpdateAgentState(this);
        DF_HELPER.registrarServicio(this);
        DF_HELPER.BI_CreacionFinalizada(this);
    }

    public void cargarInformacionAgente(ArrayList<Object> listaArgumentos) {
        this.enabled = true;
        System.out.println(listaArgumentos);
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

    public void BR_ConsultProposedSupply() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP),
                MessageTemplate.MatchConversationId(DF_HELPER.IC_CONSULT_PROPOSED_SUPPLY));

        this.addBehaviour(new ContractNetResponder(this, template) {
            protected ACLMessage handleCfp(ACLMessage cfp) {
                // getMaterialStock().repartirOptimizado(Integer.parseInt(cfp.getContent()));
                DF_HELPER.println(this.getAgent(), cfp);
                ACLMessage reply = cfp.createReply();
                if (getEnabled()) {
                    SupplyActivityRequired requiredSupply = (SupplyActivityRequired) new Gson()
                            .fromJson(cfp.getContent(), SupplyActivityRequired.class).clone();
                    MaterialStock materialStock = getMaterialStock()
                            .getOptimeCombination(requiredSupply.getCantidadPersonas());
                    SupplyActivityProposed proposedSupply = new SupplyActivityProposed(materialStock, getUbication(),
                            this.getAgent().getLocalName());
                    if (proposedSupply.getMaterialStock().getCantidadTotal() > 0) {
                        reply.setPerformative(ACLMessage.PROPOSE);
                        reply.setContent(new Gson().toJson(proposedSupply));
                    } else {
                        reply.setPerformative(ACLMessage.FAILURE);
                    }
                } else {
                    reply.setPerformative(ACLMessage.FAILURE);
                }
                return reply;
            }

            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
                System.out.println("ResponderAgent: ACCEPT_PROPOSAL received from InitiatorAgent: "
                        + accept.getSender().getName());
                ACLMessage reply = accept.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent("Proposal accepted!");
                return reply;
            }

            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                System.out.println("ResponderAgent: REJECT_PROPOSAL received from InitiatorAgent: "
                        + reject.getSender().getName());
            }
        });
    }

    public void BR_ConfirmSupplyActivity() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId(DF_HELPER.IC_CONFIRM_SUPPLY_ACTIVITY));

        this.addBehaviour(new AchieveREResponder(this, template) {
            protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
                SupplyActivity requiredSupply = (SupplyActivity) new Gson()
                        .fromJson(request.getContent(), SupplyActivity.class);
                listSupplyActivities.add(new SupplyActivityOrder(requiredSupply));
                DF_HELPER.println(this.myAgent, request);
                return request.createReply();
            }
        });
    }

    public DFHelper getDF_HELPER() {
        return DF_HELPER;
    }

    public MaterialStock getMaterialStock() {
        return materialStock;
    }

    public void setMaterialStock(MaterialStock materialStock) {
        this.materialStock = materialStock;
    }

    public Ubication getUbication() {
        return ubication;
    }

    public void setUbication(Ubication ubication) {
        this.ubication = ubication;
    }

    public ArrayList<SupplyActivityOrder> getListSupplyActivities() {
        return listSupplyActivities;
    }

    public void setListSupplyActivities(ArrayList<SupplyActivityOrder> listSupplyActivities) {
        this.listSupplyActivities = listSupplyActivities;
    }
}
