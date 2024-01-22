package src.modelsAgents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;
import src.commons.ParametersConfig;
import src.models.MaterialStock;
import src.models.SupplyActivity;
import src.models.SupplyActivityRequired;
import src.models.SupplyActivityOrder;
import src.models.Ubication;
import com.google.gson.Gson;

public class DistributionArea extends Agent implements CommonAgent{
    private final DFHelper DF_HELPER = DFHelper.getInstance();
    String IdCamion, PuntoInicial;
    long Capacidad, VelocidadVacio, VelocidadCargado, DuracionDescargas, TiempoOperacion, TotalTransportado,
            DuracionTotalViaje, DuracionAculatamiento;
    private Boolean enabled;
    private MaterialStock materialStock;
    private Integer poblacion;
    private Ubication ubication;
    private ArrayList<SupplyActivityOrder> listSupplyActivities;
    private ArrayList<Object> listaArgumentos;

    @Override
    protected void setup() {
        listaArgumentos = new ArrayList<>(Arrays.asList(getArguments()));
        this.cargarInformacionAgente();
        // RESPONDERS
        this.BR_RequiredSupply();
        this.BR_ConfirmSupplyActivity();
        this.BR_UpdateTimeEvent();
        DF_HELPER.BR_UpdateAgentState(this);
        DF_HELPER.BR_ReInitializeData(this);
        DF_HELPER.registrarServicio(this);
        // INITIATORS
        DF_HELPER.BI_CreacionFinalizada(this);

        // helper.registrarServicio(this);
        // send(helper.mensajeCreacionFinalizada(this));
        // ejecutarFuncion();
    }

    public void cargarInformacionAgente() {
        this.setPoblacion(Integer.parseInt((String) listaArgumentos.get(0)));
        this.setUbication(new Ubication(Double.parseDouble((String) listaArgumentos.get(1)),
                Double.parseDouble((String) listaArgumentos.get(2))));
        this.enabled = true;
        ArrayList<Integer> listStockMaterial = new ArrayList<>(
                Arrays.asList(Integer.parseInt((String) listaArgumentos.get(8)),
                        Integer.parseInt((String) listaArgumentos.get(7)),
                        Integer.parseInt((String) listaArgumentos.get(6)),
                        Integer.parseInt((String) listaArgumentos.get(5)),
                        Integer.parseInt((String) listaArgumentos.get(4)),
                        Integer.parseInt((String) listaArgumentos.get(3))));
        this.setMaterialStock(new MaterialStock(listStockMaterial));
        listSupplyActivities = new ArrayList<>();
    }

    public void BR_UpdateTimeEvent() {
        MessageTemplate template = DF_HELPER.MESSAGE_TEMPLATE_UPDATE_TIME_EVENT;
        addBehaviour(new AchieveREResponder(this, template) {
            ArrayList<SupplyActivityOrder> listSupplyActivities;
            MaterialStock materialStock;

            public void getValues() {
                this.listSupplyActivities = getListSupplyActivities();
                this.materialStock = getMaterialStock();
            }

            protected ACLMessage handleRequest(ACLMessage request) {
                getValues();
                ArrayList<SupplyActivityOrder> listCopySupplyActivities = this.listSupplyActivities.stream()
                        .filter(element -> !Objects.equals(element.getStatus(),
                                ParametersConfig.STATE_SUPPLY_ACTIVITY_DONE))
                        .collect(Collectors.toCollection(ArrayList::new));
                long currTime = Long.valueOf(request.getContent());

                for (SupplyActivityOrder supplyActivityOrder : listCopySupplyActivities) {
                    if (currTime >= supplyActivityOrder.getHoraFinDescarga()) {
                        supplyActivityOrder.setStatus(ParametersConfig.STATE_SUPPLY_ACTIVITY_DONE);
                        // System.out.println(this.materialStock.toString());
                        this.materialStock.addMaterialStock(supplyActivityOrder.getMaterialStock());
                        // System.out.println(this.materialStock.toString());
                        // System.out.println("ln96");

                    } else if (currTime >= supplyActivityOrder.getHoraInicioDescarga() &&
                            currTime < supplyActivityOrder.getHoraFinDescarga()) {
                        supplyActivityOrder.setStatus(ParametersConfig.STATE_SUPPLY_ACTIVITY_DOING);
                    } else {
                        supplyActivityOrder.setStatus(ParametersConfig.STATE_SUPPLY_ACTIVITY_PENDING);
                    }
                }
                // System.out.println(this.materialStock.toString());
                this.materialStock.discountMaterialStockByTime();
                // System.out.println(this.materialStock.toString());
                return new ACLMessage(ACLMessage.INFORM);
            }
        });
    }

    public void BR_RequiredSupply() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP),
                MessageTemplate.MatchConversationId(DF_HELPER.IC_CONSULT_REQUIRED_SUPPLY));
        this.addBehaviour(new ContractNetResponder(this, template) {
            private Ubication ubication;
            private Integer poblacion;
            private ArrayList<SupplyActivityOrder> listSupplyActivities;
            private Boolean enabled;
            private MaterialStock materialStock;
            private int nHelpNeed;

            public void getValues() {
                this.enabled = getEnabled();
                this.poblacion = getPoblacion();
                this.ubication = getUbication();
                this.materialStock = getMaterialStock();
                this.listSupplyActivities = getListSupplyActivities();
            }

            // @Override
            // public void onStart() {
            // this.enabled = getEnabled();
            // this.poblacion = getPoblacion();
            // this.ubication = getUbication();
            // this.materialStock = getMaterialStock();
            // this.listSupplyActivities = getListSupplyActivities();
            // super.onStart();
            // }

            protected ACLMessage handleCfp(ACLMessage cfp) {
                getValues();
                DF_HELPER.println(this.getAgent(), cfp);
                ACLMessage reply = cfp.createReply();
                if (this.enabled) {
                    long tiempoInicio = Long.valueOf(cfp.getContent());
                    // this.nHelpNeed = this.poblacion -
                    // this.materialStock.getTotalAmountHelpByPerson();
                    // System.out.println(this.materialStock.toString());
                    if (this.materialStock.getNeedHelp(this.poblacion)) {
                        this.nHelpNeed = this.materialStock.getTotalNeedHelp(this.poblacion);
                        System.out.println(this.nHelpNeed);
                        if (this.listSupplyActivities.isEmpty()) {
                            SupplyActivityRequired requiredSupply = new SupplyActivityRequired(this.poblacion,
                                    this.nHelpNeed, this.ubication, this.myAgent.getLocalName(), tiempoInicio,
                                    this.materialStock);
                            reply.setPerformative(ACLMessage.PROPOSE);
                            reply.setContent(new Gson().toJson(requiredSupply));
                        } else {
                            SupplyActivityOrder supplyActivityOrder = this.listSupplyActivities
                                    .get(this.listSupplyActivities.size() - 1);
                            System.out.println(supplyActivityOrder.toString());
                            if (tiempoInicio >= supplyActivityOrder.getHoraFinDescarga()) {
                                SupplyActivityRequired requiredSupply = new SupplyActivityRequired(this.poblacion,
                                        this.nHelpNeed, this.ubication, this.myAgent.getLocalName(), tiempoInicio,
                                        this.materialStock);
                                reply.setPerformative(ACLMessage.PROPOSE);
                                reply.setContent(new Gson().toJson(requiredSupply));
                            } else {
                                reply.setPerformative(ACLMessage.REFUSE);
                            }
                        }
                    } else {
                        reply.setPerformative(ACLMessage.REFUSE);
                    }

                    // System.out.println(this.poblacion);
                    // System.out.println(this.materialStock.getTotalAmountHelpByPerson());
                    // System.out.println("-----");
                    // if (this.listSupplyActivities.isEmpty()) {
                    // SupplyActivityRequired requiredSupply = new
                    // SupplyActivityRequired(this.poblacion,
                    // this.ubication, this.myAgent.getLocalName(), tiempoInicio,
                    // this.materialStock);
                    // reply.setPerformative(ACLMessage.PROPOSE);
                    // reply.setContent(new Gson().toJson(requiredSupply));
                    // } else {
                    // SupplyActivityOrder supplyActivityOrder = this.listSupplyActivities
                    // .get(this.listSupplyActivities.size() - 1);
                    // if (tiempoInicio >= supplyActivityOrder.getHoraFinDescarga()) {
                    // SupplyActivityRequired requiredSupply = new
                    // SupplyActivityRequired(this.poblacion,
                    // this.ubication, this.myAgent.getLocalName(), tiempoInicio,
                    // this.materialStock);
                    // reply.setPerformative(ACLMessage.PROPOSE);
                    // reply.setContent(new Gson().toJson(requiredSupply));
                    // } else {
                    // reply.setPerformative(ACLMessage.FAILURE);
                    // }
                    // // System.out.println("EL DISTRIBUTION AREA YA TIENE ALGUNA ACTIVIDAD, DEBE
                    // // TRABAJARLA");
                    // // System.exit(0);
                    // }

                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
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

    }

    public void BR_ConfirmSupplyActivity() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId(DF_HELPER.IC_CONFIRM_SUPPLY_ACTIVITY));
        this.addBehaviour(new AchieveREResponder(this, template) {
            // private MaterialStock materialStock;
            private ArrayList<SupplyActivityOrder> listSupplyActivities;

            @Override
            public void onStart() {
                // this.materialStock = getMaterialStock();
                this.listSupplyActivities = getListSupplyActivities();
                super.onStart();
            }

            protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
                SupplyActivity supplyActivity = (SupplyActivity) new Gson()
                        .fromJson(request.getContent(), SupplyActivity.class);
                SupplyActivityOrder supplyActivityOrder = supplyActivity.getSupplyActivityOrder();
                this.listSupplyActivities.add(supplyActivityOrder);
                // this.materialStock.addMaterialStock(supplyActivity.getSupplyActivityProposed().getMaterialStock());
                DF_HELPER.println(this.myAgent, request);
                return request.createReply();
            }

            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                DF_HELPER.println(this.getAgent(), reject);
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
}
