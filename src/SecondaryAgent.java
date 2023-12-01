package src;
import jade.core.Agent;

public class SecondaryAgent extends Agent {

    protected void setup() {
        System.out.println("Hola, soy un agente secundario.");
    }

    protected void takeDown() {
        System.out.println("Adiós, agente secundario finalizando.");
    }

}
