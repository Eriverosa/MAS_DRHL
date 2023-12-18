package src.modelsAgents;

import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

public class ContractNetResponderAgent extends Agent {
    protected void setup() {
        System.out.println("ContractNetResponderAgent " + getAID().getName() + " is ready.");
        int test = 3;
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