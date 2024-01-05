package src.behaviours;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public abstract class SimpleResponder extends CyclicBehaviour {
    protected MessageTemplate template;

    public SimpleResponder(Agent a, MessageTemplate mt) {
        super(a);
        this.template = mt;
    }

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive(template);
        if (msg != null) {
            handleAclMessage(msg);
        } else {
            block();
        }
    }

    protected abstract void handleAclMessage(ACLMessage msg);
}
