package src.modelsAgents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.google.gson.Gson;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;
import src.models.SupplyActivityTransportation;
import src.models.SupplyActivityOrder;
import src.commons.ParametersConfig;
import src.models.SupplyActivity;
import src.models.TransportationActivity;
import src.models.TransportationActivityItinerary;
import src.models.Ubication;

public class Truck extends Agent {
    private final DFHelper DF_HELPER = DFHelper.getInstance();
    private String IdCamion, PuntoInicial, NameTransportista;
    private long VelocidadVacio, VelocidadCargado, DuracionDescargas, TiempoOperacion, TotalTransportado,
            DuracionTotalViaje, DuracionAculatamiento;
    private Integer Capacidad;
    private ArrayList<TransportationActivity> listTransportationActivities = new ArrayList<>();
    private ArrayList<SupplyActivityOrder> listSupplyActivities = new ArrayList<>();

    public long getCapacidad() {
        return Capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        Capacidad = capacidad;
    }

    private Boolean enabled;
    TransportationActivityItinerary transportationActivityItinerary = new TransportationActivityItinerary();
    Ubication ubication;

    public Ubication getUbication() {
        return ubication;
    }

    public void setUbication(Ubication ubication) {
        this.ubication = ubication;
    }

    public TransportationActivityItinerary getTransportationActivityItinerary() {
        return transportationActivityItinerary;
    }

    public void setTransportationActivityItinerary(TransportationActivityItinerary transportationActivityItinerary) {
        this.transportationActivityItinerary = transportationActivityItinerary;
    }

    @Override
    protected void setup() {
        ArrayList<Object> listaArgumentos = new ArrayList<>(Arrays.asList(getArguments()));
        this.cargarInformacionAgente(listaArgumentos);
        this.BR_ConfirmSupplyActivity();
        DF_HELPER.BR_UpdateAgentState(this);
        DF_HELPER.registrarServicio(this);
        this.BR_ConsultFreight();
        this.BI_RegisterCarrier();
        // DF_HELPER.BI_CreacionFinalizada(this);
    }

    public void cargarInformacionAgente(ArrayList<Object> listaArgumentos) {
        this.enabled = true;
        this.setNameTransportista(listaArgumentos.get(0).toString());
        this.setUbication(new Ubication(Integer.parseInt((String) listaArgumentos.get(1)),
                Integer.parseInt((String) listaArgumentos.get(2))));
        System.out.println(listaArgumentos.get(3));
        this.setCapacidad((Integer) listaArgumentos.get(3));
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

    public String getNameTransportista() {
        return NameTransportista;
    }

    public void setNameTransportista(String name) {
        this.NameTransportista = name;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public void BI_RegisterCarrier() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setConversationId(DF_HELPER.IC_REGISTER_TO_CARRIER);
        msg.addReceiver(DF_HELPER.getAgent(getNameTransportista()).getAID());
        msg.setContent(this.getLocalName());
        this.addBehaviour(new AchieveREInitiator(this, msg) {
            @Override
            protected void handleAgree(ACLMessage agree) {
                onEnd();
            }

            @Override
            public int onEnd() {
                super.onEnd();
                System.out.println("Agente " + this.getAgent().getLocalName() + ": se registra su creación.");
                BI_CreacionFinalizada();
                return 0;
            }
        });
    }

    public void BI_CreacionFinalizada() {
        DF_HELPER.BI_CreacionFinalizada(this);
    }

    public void BR_ConsultFreight() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP),
                MessageTemplate.MatchConversationId(DF_HELPER.IC_REQUEST_FREIGHT));
        this.addBehaviour(new ContractNetResponder(this, template) {
            ArrayList<SupplyActivityOrder> listSupplyActivities;

            @Override
            public void onStart() {
                this.listSupplyActivities = getListSupplyActivities();
                // TODO Auto-generated method stub
                super.onStart();
            }

            protected ACLMessage handleCfp(ACLMessage cfp) {
                DF_HELPER.println(this.getAgent(), cfp);
                ACLMessage reply = cfp.createReply();
                if (getEnabled()) {
                    if (searchRoute()) {
                        SupplyActivity supplyActivity = (SupplyActivity) new Gson()
                                .fromJson(cfp.getContent(), SupplyActivity.class);
                        long tiempoViajeCarga = getTravelTime(getUbication(),
                                supplyActivity.getSupplyActivityProposed().getUbicacion());
                        long tiempoViajeDescarga = getTravelTime(
                                supplyActivity.getSupplyActivityRequired().getUbicacion(),
                                getUbication());
                        Integer cantidadCarga = getMaxLoad((int) getCapacidad(),
                                supplyActivity.getSupplyActivityProposed().getMaterialStock().getTotalAmountHelp());
                        long horaInicioDisponible = getEnabledActivityTime(
                                supplyActivity.getSupplyActivityRequired().getHoraRequerida(), tiempoViajeCarga);
                        if (ParametersConfig.ERROR_LONG != horaInicioDisponible) {
                            SupplyActivityTransportation proposedTransportationActivity = new SupplyActivityTransportation(
                                    horaInicioDisponible,
                                    tiempoViajeCarga, tiempoViajeDescarga, this.getAgent().getLocalName(),
                                    cantidadCarga,
                                    supplyActivity.getSupplyActivityProposed().getAgentName(),
                                    supplyActivity.getSupplyActivityRequired().getAgentName());
                            reply.setContent(new Gson().toJson(proposedTransportationActivity));
                            reply.setPerformative(ACLMessage.PROPOSE);
                        } else {
                            reply.setPerformative(ACLMessage.REFUSE);
                        }
                        // } else {
                        // System.out.println("EL TRUCK YA TIENE ALGUNA ACTIVIDAD, DEBE TRABAJARLA");
                        // System.exit(0);
                        // }
                    } else {
                        reply.setPerformative(ACLMessage.REFUSE);
                    }
                } else {
                    reply.setPerformative(ACLMessage.FAILURE);
                }
                return reply;
            }

            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
                DF_HELPER.println(this.getAgent(), accept);
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
            ArrayList<SupplyActivityOrder> listSupplyActivities;

            @Override
            public void onStart() {
                this.listSupplyActivities = getListSupplyActivities();
                super.onStart();
            }

            protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
                SupplyActivity requiredSupply = (SupplyActivity) new Gson()
                        .fromJson(request.getContent(), SupplyActivity.class);
                this.listSupplyActivities.add(new SupplyActivityOrder(requiredSupply));
                DF_HELPER.println(this.myAgent, request);
                return request.createReply();
            }
        });
    }

    public boolean searchRoute() {
        Random random = new Random();
        return random.nextBoolean();
    }

    // public static long getInitTime(long l, long tiempoInicioDisponible, long
    // tiempoViajeCarga) {
    // return Math.max(l, tiempoInicioDisponible + tiempoViajeCarga);
    // }

    public static long getTravelTime(Ubication supplyActivityUbication, Ubication proposedActivityUbication) {
        // Radio medio de la Tierra en kilómetros
        double radioTierra = 6371;

        // Diferencias de latitud y longitud
        double dLat = proposedActivityUbication.getLatitud() - supplyActivityUbication.getLatitud();
        double dLon = proposedActivityUbication.getLongitud() - supplyActivityUbication.getLongitud();

        // Fórmula de la distancia de Haverseno
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(supplyActivityUbication.getLatitud())
                        * Math.cos(proposedActivityUbication.getLatitud()) * Math.sin(dLon / 2)
                        * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Distancia en kilómetros
        double distancia = radioTierra * c;

        // Velocidad constante en metros por segundo
        long velocidadMetrosPorSegundo = 50 * 1000; // 50 km/h = 50,000 m/h = 50,000 m/3,600 s = 50 * 1000 m/s

        // Calcular el tiempo de viaje en milisegundos
        long tiempoEnMilisegundos = (long) ((distancia * 1000) / velocidadMetrosPorSegundo); // Convertir a milisegundos
        return tiempoEnMilisegundos;
    }

    public static int getMaxLoad(int capacidadCamion, int pesoCargar) {
        if (capacidadCamion <= 0 || pesoCargar <= 0) {
            return 0;
        }

        int cargaMaxima = Math.min(capacidadCamion, pesoCargar);

        return cargaMaxima;
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

    public ArrayList<TransportationActivity> getListTransportationActivities() {
        return listTransportationActivities;
    }

    public void setListTransportationActivities(ArrayList<TransportationActivity> listTransportationActivities) {
        this.listTransportationActivities = listTransportationActivities;
    }

    public ArrayList<SupplyActivityOrder> getListSupplyActivities() {
        return listSupplyActivities;
    }

    public void setListSupplyActivities(ArrayList<SupplyActivityOrder> listSupplyActivities) {
        this.listSupplyActivities = listSupplyActivities;
    }

    public long getEnabledActivityTime(long timeRequired, long tiempoViajeCarga) {
        if (!this.listSupplyActivities.isEmpty()) {
            SupplyActivityOrder supplyActivityOrder = this.listSupplyActivities
                    .get(this.listSupplyActivities.size() - 1);
            if (timeRequired <= supplyActivityOrder.getHoraFinDescarga()) {
                return ParametersConfig.ERROR_LONG;
            } else {
                return Math.max(timeRequired, supplyActivityOrder.getHoraFinDescarga() + tiempoViajeCarga);
            }
        } else {
            return timeRequired;
        }
    }

}
