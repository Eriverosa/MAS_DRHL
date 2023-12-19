package src.modelsAgents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;

public class ContractNetInitiatorAgent extends Agent {
    protected void setup() {
        System.out.println("ContractNetInitiatorAgent " + getAID().getName() + " is ready.");
        int test = 3;
        ACLMessage cfp = new ACLMessage();
        cfp.addReceiver(new AID("Responder_0", AID.ISLOCALNAME));
        if (test == 3) {
            for (int i = 0; i < 5; i++) {
                cfp.setPerformative(ACLMessage.INFORM);
                cfp.addReceiver(new AID("Receptor" + i, AID.ISLOCALNAME));
                cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                System.out.println(i);
                cfp.setContent("Contenido del mensaje " + i);
                send(cfp);
            }
        }
        if (test == 2) {
            for (int i = 0; i < 5; i++) {
                cfp.setPerformative(ACLMessage.INFORM);
                // Configura el destinatario, protocolo, etc., según tus necesidades
                cfp.addReceiver(new AID("Receptor" + i, AID.ISLOCALNAME));
                cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);

                // Agrega cualquier contenido adicional al mensaje
                cfp.setContent("Contenido del mensaje " + i);

                // Agrega el comportamiento ContractNetInitiator para este mensaje
                this.addBehaviour(new ContractNetInitiator(this, cfp) {
                    // Define los métodos necesarios aquí
                    // handleAllResponses, handleRefuse, etc.
                });
            }
        } else if (test == 1) {

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
}
