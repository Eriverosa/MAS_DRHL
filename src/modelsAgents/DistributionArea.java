package src.modelsAgents;

import java.util.ArrayList;
import java.util.Arrays;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;
import src.models.MaterialStock;
import src.models.SupplyActivity;
import src.models.SupplyActivityRequired;
import src.models.SupplyActivityOrder;
import src.models.Ubication;
import com.google.gson.Gson;

public class DistributionArea extends Agent {
    private final DFHelper DF_HELPER = DFHelper.getInstance();
    String IdCamion, PuntoInicial;
    long Capacidad, VelocidadVacio, VelocidadCargado, DuracionDescargas, TiempoOperacion, TotalTransportado,
            DuracionTotalViaje, DuracionAculatamiento;
    private Boolean enabled;
    private MaterialStock materialStock;
    private Integer poblacion;
    private Ubication ubication;
    private ArrayList<SupplyActivityOrder> listSupplyActivities = new ArrayList<>();

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

    public MaterialStock getMaterialStock() {
        return materialStock;
    }

    public void setMaterialStock(MaterialStock materialStock) {
        this.materialStock = materialStock;
    }

    @Override
    protected void setup() {
        ArrayList<Object> listaArgumentos = new ArrayList<>(Arrays.asList(getArguments()));
        // this.cargarInformacionAgente(listaArgumentos);
        this.BR_RequiredSupply();
        this.BR_ConfirmSupplyActivity();
        this.cargarInformacionAgente(listaArgumentos);
        DF_HELPER.BR_UpdateAgentState(this);
        DF_HELPER.registrarServicio(this);
        DF_HELPER.BI_CreacionFinalizada(this);
        // helper.registrarServicio(this);
        // send(helper.mensajeCreacionFinalizada(this));
        // ejecutarFuncion();
    }

    public void cargarInformacionAgente(ArrayList<Object> listaArgumentos) {
        this.setPoblacion(Integer.parseInt((String) listaArgumentos.get(0)));
        this.setUbication(new Ubication(Integer.parseInt((String) listaArgumentos.get(1)),
                Integer.parseInt((String) listaArgumentos.get(2))));
        this.enabled = true;
        ArrayList<Integer> listStockMaterial = new ArrayList<>(
                Arrays.asList(Integer.parseInt((String) listaArgumentos.get(8)),
                        Integer.parseInt((String) listaArgumentos.get(7)),
                        Integer.parseInt((String) listaArgumentos.get(6)),
                        Integer.parseInt((String) listaArgumentos.get(5)),
                        Integer.parseInt((String) listaArgumentos.get(4)),
                        Integer.parseInt((String) listaArgumentos.get(3))));
        this.setMaterialStock(new MaterialStock(listStockMaterial));
    }

    public void BR_RequiredSupply() {
        int testValue = 2;
        if (testValue == 2) {
            MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP),
                    MessageTemplate.MatchConversationId(DF_HELPER.IC_CONSULT_REQUIRED_SUPPLY));
            this.addBehaviour(new ContractNetResponder(this, template) {
                private Ubication ubication;
                private Integer poblacion;
                private ArrayList<SupplyActivityOrder> listSupplyActivities;

                @Override
                public void onStart() {
                    this.poblacion = getPoblacion();
                    this.ubication = getUbication();
                    this.listSupplyActivities = getListSupplyActivities();
                    super.onStart();
                }

                protected ACLMessage handleCfp(ACLMessage cfp) {
                    DF_HELPER.println(this.getAgent(), cfp);
                    ACLMessage reply = cfp.createReply();
                    if (getEnabled()) {
                        if (this.listSupplyActivities.isEmpty()) {
                            long tiempoInicio = Long.valueOf(cfp.getContent());
                            SupplyActivityRequired requiredSupply = new SupplyActivityRequired(this.poblacion,
                                    this.ubication, this.myAgent.getLocalName(), tiempoInicio);
                            reply.setPerformative(ACLMessage.PROPOSE);
                            reply.setContent(new Gson().toJson(requiredSupply));
                        } else {
                            System.out.println("EL DISTRIBUTION AREA YA TIENE ALGUNA ACTIVIDAD, DEBE TRABAJARLA");
                            System.exit(0);
                        }

                    } else {
                        reply.setPerformative(ACLMessage.FAILURE);
                    }
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
        } else if (testValue == 1) {
            MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP),
                    MessageTemplate.MatchConversationId(DF_HELPER.IC_CONSULT_REQUIRED_SUPPLY));

            this.addBehaviour(new ContractNetResponder(this, template) {
                private Integer poblacion;
                private Ubication ubication;

                @Override
                public void onStart() {
                    this.poblacion = getPoblacion();
                    this.ubication = getUbication();
                    super.onStart();
                }

                protected ACLMessage handleCfp(ACLMessage cfp) {
                    // getMaterialStock().repartirOptimizado(Integer.parseInt(cfp.getContent()));
                    DF_HELPER.println(this.getAgent(), cfp);
                    ACLMessage reply = cfp.createReply();
                    if (getEnabled()) {
                        long tiempoInicio = 0;
                        if (!listSupplyActivities.isEmpty()) {
                            System.exit(0);
                        }
                        SupplyActivityRequired requiredSupply = new SupplyActivityRequired(this.poblacion,
                                this.ubication, this.myAgent.getLocalName(), tiempoInicio);
                        reply.setPerformative(ACLMessage.PROPOSE);
                        reply.setContent(new Gson().toJson(requiredSupply));
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

    }

    public void BR_ConfirmSupplyActivity() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId(DF_HELPER.IC_CONFIRM_SUPPLY_ACTIVITY));
        this.addBehaviour(new AchieveREResponder(this, template) {
            private MaterialStock materialStock;

            @Override
            public void onStart() {
                this.materialStock = getMaterialStock();
                super.onStart();
            }

            protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
                SupplyActivity supplyActivity = (SupplyActivity) new Gson()
                        .fromJson(request.getContent(), SupplyActivity.class);
                this.materialStock.addMaterialStock(supplyActivity.getSupplyActivityProposed().getMaterialStock());
                DF_HELPER.println(this.myAgent, request);
                return request.createReply();
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

    public String getIdCamion() {
        return IdCamion;
    }

    public void setIdCamion(String idCamion) {
        IdCamion = idCamion;
    }

    public String getPuntoInicial() {
        return PuntoInicial;
    }

    public void setPuntoInicial(String puntoInicial) {
        PuntoInicial = puntoInicial;
    }

    public long getCapacidad() {
        return Capacidad;
    }

    public void setCapacidad(long capacidad) {
        Capacidad = capacidad;
    }

    public long getVelocidadVacio() {
        return VelocidadVacio;
    }

    public void setVelocidadVacio(long velocidadVacio) {
        VelocidadVacio = velocidadVacio;
    }

    public long getVelocidadCargado() {
        return VelocidadCargado;
    }

    public void setVelocidadCargado(long velocidadCargado) {
        VelocidadCargado = velocidadCargado;
    }

    public long getDuracionDescargas() {
        return DuracionDescargas;
    }

    public void setDuracionDescargas(long duracionDescargas) {
        DuracionDescargas = duracionDescargas;
    }

    public long getTiempoOperacion() {
        return TiempoOperacion;
    }

    public void setTiempoOperacion(long tiempoOperacion) {
        TiempoOperacion = tiempoOperacion;
    }

    public long getTotalTransportado() {
        return TotalTransportado;
    }

    public void setTotalTransportado(long totalTransportado) {
        TotalTransportado = totalTransportado;
    }

    public long getDuracionTotalViaje() {
        return DuracionTotalViaje;
    }

    public void setDuracionTotalViaje(long duracionTotalViaje) {
        DuracionTotalViaje = duracionTotalViaje;
    }

    public long getDuracionAculatamiento() {
        return DuracionAculatamiento;
    }

    public void setDuracionAculatamiento(long duracionAculatamiento) {
        DuracionAculatamiento = duracionAculatamiento;
    }

    public ArrayList<SupplyActivityOrder> getListSupplyActivities() {
        return listSupplyActivities;
    }

    public void setListSupplyActivities(ArrayList<SupplyActivityOrder> listSupplyActivities) {
        this.listSupplyActivities = listSupplyActivities;
    }
}
