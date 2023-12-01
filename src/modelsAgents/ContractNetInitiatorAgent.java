package src.modelsAgents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
// import jade.core.behaviours.CyclicBehaviour;

import java.util.Vector;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;

public class ContractNetInitiatorAgent extends Agent {
    protected void setup() {
        System.out.println("ContractNetInitiatorAgent " + getAID().getName() + " is ready.");

        addBehaviour(new OneShotBehaviour() {
            public void action() {
                // Crear el mensaje de saludo
                ACLMessage saludo = new ACLMessage(ACLMessage.INFORM);

                // Agregar los receptores (respondedores) del saludo
                saludo.addReceiver(new AID("Respondedor1", AID.ISLOCALNAME));
                saludo.addReceiver(new AID("Respondedor2", AID.ISLOCALNAME));
                saludo.addReceiver(new AID("Respondedor3", AID.ISLOCALNAME));
                saludo.addReceiver(new AID("Respondedor4", AID.ISLOCALNAME));
                saludo.addReceiver(new AID("Respondedor5", AID.ISLOCALNAME));
                System.out.println("Hola como están ?");
                saludo.setContent("¡Hola! ¿Cómo estás?");
                saludo.setConversationId("saludo-conversation");
                saludo.setReplyWith("saludo" + System.currentTimeMillis());
                myAgent.send(saludo);

                MessageTemplate mt = MessageTemplate.and(
                        MessageTemplate.MatchConversationId("saludo-conversation"),
                        MessageTemplate.MatchInReplyTo(saludo.getReplyWith()));

                // Comportamiento del manejador de respuestas
                addBehaviour(new CyclicBehaviour() {
                    public void action() {
                        ACLMessage respuesta = myAgent.receive(mt);
                        if (respuesta != null) {
                            // Procesar la respuesta recibida del respondedor
                            System.out.println("Recibí el saludo: " + respuesta.getContent());
                        } else {
                            block();
                        }
                    }
                });
            }
        });
    }
}
