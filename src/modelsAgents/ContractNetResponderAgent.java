package src.modelsAgents;

import com.google.gson.Gson;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;
import src.commons.ParametersConfig;

public class ContractNetResponderAgent extends Agent {
    private final DFHelper DF_HELPER = DFHelper.getInstance();

    protected void setup() {
        System.out.println("ContractNetResponderAgent " + getAID().getName() + " is ready.");
        int test = 10;
        if (ParametersConfig.N_TEST == 7) {
            MessageTemplate template = DF_HELPER.MESSAGE_TEMPLATE_INITIALIZE_SIMULATION;
            addBehaviour(new CyclicBehaviour(this) {
                public void action() {
                    ACLMessage msg = myAgent.receive(template);
                    if (msg != null) {
                        System.out.println("Llegó el mensaje");
                    } else {
                        block();
                    }
                }
            });
        }
        if (test == 5) {
            addBehaviour(new AchieveREResponder(this,
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET)) {
                protected ACLMessage handleRequest(ACLMessage request) {
                    System.out.println("Agente " + getLocalName() + " ha recibido una solicitud de información: "
                            + request.getContent());

                    ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                    inform.setContent("Respuesta del Responder.");
                    inform.addReceiver(request.getSender());
                    return inform;
                }
            });
        }
        if (test == 4) {
            addBehaviour(new ContractNetResponder(this,
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET)) {
                protected ACLMessage handleCfp(ACLMessage cfp) {
                    System.out.println("Agente " + getLocalName() + " ha recibido un mensaje INFORM del Initiator.");
                    ACLMessage inform = cfp.createReply();
                    inform.setPerformative(ACLMessage.INFORM);
                    inform.setContent("Respuesta del Responder.");
                    return inform;
                }
            });

        }
        if (test == 3) {
            this.addBehaviour(new ContractNetResponder(this,
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET)) {
                protected ACLMessage handleCfp(ACLMessage cfp) {
                    // DF_HELPER.println(myAgent, "Llegó al final de la simulación");
                    System.out.println("CFP recibido: " + cfp.getContent());
                    return null;
                }
            });
        }
        if (test == 2) {
            this.addBehaviour(new ContractNetResponder(this,
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET)) {
                protected ACLMessage handleCfp(ACLMessage cfp) {
                    // Procesa el CFP según tus necesidades
                    System.out.println("CFP recibido: " + cfp.getContent());

                    // No envía ninguna respuesta
                    return null;
                }
            });
        } else if (test == 1) {
            addBehaviour(new CyclicBehaviour() {
                public void action() {
                    MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                    ACLMessage saludo = myAgent.receive(mt);
                    if (saludo != null) {
                        // Procesar el saludo recibido del iniciador
                        System.out.println("Recibí el saludo: " + saludo.getContent());
                        // Enviar una respuesta al iniciador
                        ACLMessage respuesta = saludo.createReply();
                        respuesta.setPerformative(ACLMessage.INFORM);
                        respuesta.setContent("¡Hola! Estoy bien, gracias.");
                        myAgent.send(respuesta);
                    } else {
                        block();
                    }
                }
            });
        }
    }
}