package src.modelsAgents;

import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

public class ContractNetResponderAgent extends Agent {
    protected void setup() {
        System.out.println("ContractNetResponderAgent " + getAID().getName() + " is ready.");

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