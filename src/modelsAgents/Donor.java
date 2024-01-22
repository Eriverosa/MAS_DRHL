package src.modelsAgents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;
import src.commons.ParametersConfig;
import src.models.MaterialStock;
import src.models.Stock;
import src.models.SupplyActivityProposed;
import src.models.SupplyActivityRequired;
import src.models.SupplyActivityOrder;
import src.models.SupplyActivity;
import src.models.Ubication;

public class Donor extends Agent implements CommonAgent {
    private final DFHelper DF_HELPER = DFHelper.getInstance();
    private Boolean enabled;
    private MaterialStock materialStock;
    private MaterialStock materialStockReserved;
    private Ubication ubication;
    private ArrayList<SupplyActivityOrder> listSupplyActivities = new ArrayList<>();
    private ArrayList<Object> listaArgumentos;

    @Override
    protected void setup() {
        listaArgumentos = new ArrayList<>(Arrays.asList(getArguments()));
        cargarInformacionAgente();
        // RESPONDERS
        this.BR_ConsultProposedSupply();
        this.BR_ConfirmSupplyActivity();
        this.BR_UpdateTimeEvent();
        DF_HELPER.BR_UpdateAgentState(this);
        DF_HELPER.BR_ReInitializeData(this);
        DF_HELPER.registrarServicio(this);
        // INITIATORS
        DF_HELPER.BI_CreacionFinalizada(this);
    }

    public void cargarInformacionAgente() {
        this.enabled = true;
        this.setUbication(new Ubication(Double.parseDouble((String) listaArgumentos.get(0)),
                Double.parseDouble((String) listaArgumentos.get(1))));
        ArrayList<Integer> listStockMaterial = new ArrayList<>(
                Arrays.asList(Integer.parseInt((String) listaArgumentos.get(7)),
                        Integer.parseInt((String) listaArgumentos.get(6)),
                        Integer.parseInt((String) listaArgumentos.get(5)),
                        Integer.parseInt((String) listaArgumentos.get(4)),
                        Integer.parseInt((String) listaArgumentos.get(3)),
                        Integer.parseInt((String) listaArgumentos.get(2))));
        this.setMaterialStock(new MaterialStock(listStockMaterial));
        this.setMaterialStockReserved(new MaterialStock());
    }

    public void BR_ConsultProposedSupply() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP),
                MessageTemplate.MatchConversationId(DF_HELPER.IC_CONSULT_PROPOSED_SUPPLY));

        this.addBehaviour(new ContractNetResponder(this, template) {
            MaterialStock materialStock;
            Boolean enabled;

            public void getValues() {
                this.enabled = getEnabled();
                this.materialStock = getMaterialStock();
            }

            protected ACLMessage handleCfp(ACLMessage cfp) {
                getValues();
                DF_HELPER.println(this.getAgent(), cfp);
                ACLMessage reply = cfp.createReply();
                // System.out.println(this.enabled);
                if (this.enabled) {
                    SupplyActivityRequired requiredSupply = (SupplyActivityRequired) new Gson()
                            .fromJson(cfp.getContent(), SupplyActivityRequired.class).clone();
                    MaterialStock materialStockSupply = this.materialStock
                            .getOptimeCombination(requiredSupply.getCantidadPersonaRequired());
                    SupplyActivityProposed proposedSupply = new SupplyActivityProposed(materialStockSupply,
                            getUbication(),
                            this.getAgent().getLocalName());
                    if (proposedSupply.getMaterialStock().getTotalAmountHelpByCC() > 0) {
                        reply.setPerformative(ACLMessage.PROPOSE);
                        reply.setContent(new Gson().toJson(proposedSupply));
                    } else {
                        reply.setPerformative(ACLMessage.REFUSE);
                    }
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                }
                return reply;
            }

            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
                DF_HELPER.println(this.getAgent(), accept);
                ACLMessage reply = accept.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                // reply.setContent("Proposal accepted!");
                return reply;
            }

            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                DF_HELPER.println(this.getAgent(), reject);
                // System.out.println("ResponderAgent: REJECT_PROPOSAL received from
                // InitiatorAgent: "
                // + reject.getSender().getName());
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
                materialStock.removeMaterialStock(requiredSupply.getSupplyActivityOrder().getMaterialStock());
                materialStockReserved.addMaterialStock(requiredSupply.getSupplyActivityOrder().getMaterialStock());
                DF_HELPER.println(this.myAgent, request);
                return request.createReply();
            }
        });
    }

    public void BR_UpdateTimeEvent() {
        MessageTemplate template = DF_HELPER.MESSAGE_TEMPLATE_UPDATE_TIME_EVENT;
        addBehaviour(new AchieveREResponder(this, template) {
            ArrayList<SupplyActivityOrder> listSupplyActivities;
            MaterialStock materialStock, materialStockReserved, materialStockInitial;

            public void getValues() {
                this.materialStock = getMaterialStock();
                this.materialStockReserved = getMaterialStockReserved();
                this.materialStockInitial = new MaterialStock(new ArrayList<>(
                        Arrays.asList(Integer.parseInt((String) listaArgumentos.get(7)),
                                Integer.parseInt((String) listaArgumentos.get(6)),
                                Integer.parseInt((String) listaArgumentos.get(5)),
                                Integer.parseInt((String) listaArgumentos.get(4)),
                                Integer.parseInt((String) listaArgumentos.get(3)),
                                Integer.parseInt((String) listaArgumentos.get(2)))));
                this.listSupplyActivities = getListSupplyActivities();
            }

            protected ACLMessage handleRequest(ACLMessage request) {
                getValues();
                ArrayList<SupplyActivityOrder> listCopySupplyActivities = this.listSupplyActivities.stream()
                        .filter(element -> !Objects.equals(element.getStatus(),
                                ParametersConfig.STATE_SUPPLY_ACTIVITY_DONE))
                        .collect(Collectors.toCollection(ArrayList::new));
                long currTime = Long.valueOf(request.getContent());
                // Integer valor1 = 0, valor2 = 0, valor3 = 0;

                for (SupplyActivityOrder supplyActivityOrder : listCopySupplyActivities) {
                    // System.out.println("Entero here");
                    System.out.println(supplyActivityOrder.toString());
                    if (currTime >= supplyActivityOrder.getHoraFinCarga()) {
                        supplyActivityOrder.setStatus(ParametersConfig.STATE_SUPPLY_ACTIVITY_DONE);
                        this.materialStockReserved.removeMaterialStock(supplyActivityOrder.getMaterialStock());
                    } else if (currTime >= supplyActivityOrder.getHoraInicioDescarga() &&
                            currTime < supplyActivityOrder.getHoraFinDescarga()) {
                        supplyActivityOrder.setStatus(ParametersConfig.STATE_SUPPLY_ACTIVITY_DOING);
                    } else {
                        supplyActivityOrder.setStatus(ParametersConfig.STATE_SUPPLY_ACTIVITY_PENDING);
                    }
                }
                double minStockPercentage = ParametersConfig.DONOR_CANTIDAD_MINIMA_STOCK_PERCENT;
                int threshold = (int) (minStockPercentage * this.materialStockInitial.getTotalAmountHelpByCC());
                if (this.materialStock.getTotalAmountHelpByCC() < threshold) {
                    this.materialStock.getMaterialStock().forEach(iterable_element1 -> {
                        this.materialStockInitial.getMaterialStock().stream()
                                .filter(iterable_element2 -> iterable_element1.getTamanho() == iterable_element2
                                        .getTamanho())
                                .findFirst()
                                .ifPresent(iterable_element2 -> {
                                    if (iterable_element1.getCantidad() < (minStockPercentage
                                            * iterable_element2.getCantidad())) {
                                        iterable_element1.setCantidad((int) (iterable_element1.getCantidad()
                                                + (minStockPercentage * iterable_element2.getCantidad())));
                                    }
                                });
                    });
                } else {
                    System.out.println("Aun queda más del porcentaje");
                }
                // if (this.materialStock.getTotalAmountHelpByCC() < (ParametersConfig.DONOR_CANTIDAD_MINIMA_STOCK_PERCENT
                //         * this.materialStockInitial.getTotalAmountHelpByCC())) {
                //     for (Stock iterable_element1 : this.materialStock.getMaterialStock()) {
                //         for (Stock iterable_element2 : this.materialStockInitial.getMaterialStock()) {
                //             if (iterable_element1.getTamanho() == iterable_element2.getTamanho()) {
                //                 if (iterable_element1
                //                         .getCantidad() < (ParametersConfig.DONOR_CANTIDAD_MINIMA_STOCK_PERCENT
                //                                 * iterable_element2.getCantidad())) {
                //                     iterable_element1.setCantidad((int) (iterable_element1.getCantidad()
                //                             + (ParametersConfig.DONOR_CANTIDAD_MINIMA_STOCK_PERCENT
                //                                     * iterable_element2.getCantidad())));
                //                 }
                //                 break;
                //             }

                //         }
                //     }
                // } else {
                //     System.out.println("Aun queda mas del porcentaje");
                // }
                // if (this.materialStock.getTotalAmountHelpByCC()) {

                // }

                // if (valor1 < (ParametersConfig.DONOR_CANTIDAD_MINIMA_STOCK_PERCENT * valor2))
                // {
                // System.out.println("Debe reabastecer");
                // DF_HELPER.println(myAgent,
                // Integer.toString(this.materialStock.getTotalAmountHelpByCC()));
                // DF_HELPER.println(myAgent,
                // Integer.toString(this.materialStockInitial.getTotalAmountHelpByCC()));
                // } else {
                // System.out.println("No es necesario");
                // }

                // ArrayList<SupplyActivityOrder> listCopySupplyActivities =
                // this.listSupplyActivities.stream()
                // .filter(element -> !Objects.equals(element.getStatus(),
                // ParametersConfig.STATE_SUPPLY_ACTIVITY_DONE))
                // .collect(Collectors.toCollection(ArrayList::new));
                // long currTime = Long.valueOf(request.getContent());

                // for (SupplyActivityOrder supplyActivityOrder : listCopySupplyActivities) {
                // if (currTime >= supplyActivityOrder.getHoraFinDescarga()) {
                // supplyActivityOrder.setStatus(ParametersConfig.STATE_SUPPLY_ACTIVITY_DONE);
                // // System.out.println(this.materialStock.toString());
                // this.materialStock.addMaterialStock(supplyActivityOrder.getMaterialStock());
                // // System.out.println(this.materialStock.toString());
                // // System.out.println("ln96");

                // } else if (currTime >= supplyActivityOrder.getHoraInicioDescarga() &&
                // currTime < supplyActivityOrder.getHoraFinDescarga()) {
                // supplyActivityOrder.setStatus(ParametersConfig.STATE_SUPPLY_ACTIVITY_DOING);
                // } else {
                // supplyActivityOrder.setStatus(ParametersConfig.STATE_SUPPLY_ACTIVITY_PENDING);
                // }
                // }
                // // System.out.println(this.materialStock.toString());
                // this.materialStock.discountMaterialStockByTime();
                // System.out.println(this.materialStock.toString());
                return new ACLMessage(ACLMessage.INFORM);
            }
        });
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

    public MaterialStock getMaterialStockReserved() {
        return materialStockReserved;
    }

    public void setMaterialStockReserved(MaterialStock materialStockReserved) {
        this.materialStockReserved = materialStockReserved;
    }
}
