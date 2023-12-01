package src;
import jade.core.Agent;

public class MainAgent extends Agent {

    protected void setup() {
        System.out.println("Hola, soy el agente principal.");
    }

    protected void takeDown() {
        System.out.println("Adiós, agente principal finalizando.");
    }

}
