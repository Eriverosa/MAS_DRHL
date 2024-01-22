package src.behaviours;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public abstract class SimpleConversationResponder extends CyclicBehaviour {
    protected MessageTemplate template;

    public SimpleConversationResponder(Agent a, MessageTemplate mt) {
        super(a);
        this.template = mt;
    }

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive(template);
        if (msg != null) {
            // Maneja el mensaje y envía una respuesta
            ACLMessage reply = handleAclMessage(msg);
            if (reply != null) {
                myAgent.send(reply);
            }
        } else {
            block();
        }
    }

    // Modificado para devolver un ACLMessage que será la respuesta
    protected abstract ACLMessage handleAclMessage(ACLMessage msg);
}
