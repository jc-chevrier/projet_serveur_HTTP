import core.HttpServer;

/**
 * Classe principale du projet.
 *
 * @author CHEVRIER Jean-Christophe, HADJ MESSAOUD Yousra, LOUGADI Marième,
 *         étudiants en MASTER 1 MIAGE, à l'université de Lorraine.
 */
public class Main {
    public static void main(String[] args) {
        //Démarrage du serveur.
        HttpServer.getInstance().start();
    }
}