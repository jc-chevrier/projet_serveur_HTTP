package core;

import org.jetbrains.annotations.NotNull;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;

/**
 * Classe pour la gestion des connexions HTTP.
 *
 * @author CHEVRIER Jean-Christophe, HADJ MESSAOUD Yousra, LOUGADI Marième,
 *         étudiants en MASTER 1 MIAGE, à l'université de Lorraine.
 */
public class HttpConnection {
    //Singleton serveur HTTP.
    private final static HttpServer httpServer = HttpServer.getInstance();
    //"Prise" sur le réseau : connexion TCP.
    private Socket socket;
    //Adresse IP du client web.
    private String ipHoteClient;
    //Flux d'entrée de la connexion.
    private BufferedReader inputStream;
    //Flux de sortie de la connexion.
    private BufferedOutputStream outputStream;
    //Dernière requête HTTP reçue.
    private HttpRequest lastHttpRequest;

    /**
     * Créer un objet core.HttpConnection
     * à partir d'une socket.
     *
     * @param socket
     */
    private HttpConnection(@NotNull Socket socket) {
        //Sauvegarde de la connexion TCP.
        this.socket = socket;
        //Sauvegarde du client web.
        ipHoteClient = socket.getInetAddress().getHostAddress();
        //Chargement du flux d'entrée de la
        //connexion TCP, pour la réception
        //des requêtes HTTP.
        loadInputStream();
        //Chargement du flux de sortie de la
        //connexion TCP, pour l'envoi des
        //réponses HTTP.
        loadOutputStream();
        //Au début pas de requête.
        lastHttpRequest = null;
    }

    /**
     * Charger le flux d'entrée de la
     * connexion TCP pour la réception
     * des requêtes HTTP.
     */
    public void loadInputStream() {
        try {
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException exception) {
           httpServer.logErrorAndExit("Erreur au chargement du flux d'entrée d'une connexion TCP !", exception);
        }
    }

    /**
     * Charger le flux de sortie de la
     * connexion TCP pour l'envoi des
     * réponses HTTP.
     */
    public void loadOutputStream() {
        try {
            outputStream = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException exception) {
            httpServer.logErrorAndExit("Erreur au chargement du flux de sortie d'une connexion TCP !", exception);
        }
    }

    /**
     * Ecouter et entretenir une connexion HTTP
     * (contexte statique).
     *
     * @param socket
     */
    public static void handle(@NotNull Socket socket) {
        //Mise en place de la connexion HTTP.
        HttpConnection httpConnection = new HttpConnection(socket);
        //Lancement de l'écoute des requêtes HTTP sur
        //la connexion.
        httpConnection.handle();
    }

    /**
     * Ecouter et entretenir une connexion HTTP.
     */
    private void handle() {
        //Attente et analyse de la dernière requête
        //HTTP reçue du client web.
        waitAndParseHttpRequest();
        //Envoi de la réponse HTTP du serveur, à la
        //dernière requête HTTP reçue du client web.
        respondToHttpRequest();
        //Si le client web veut conserver la connexion.
        if(clientKeepsConnection()) {
            //Cas récursif.
            //On boucle et on attend une nouvelle requête HTTP.
            handle();
        //Sinon.
        } else {
            //Cas triviaL.
            //On arrête la connexion TCP.
            stopTcpConnection();
        }
    }

    /**
     * Attendre et analyser la dernière requête nouvellement reçue.
     */
    private void waitAndParseHttpRequest() {
        //Lecture et récupération de la requête.
        List<String> headers = new ArrayList<String>();
        try {
            //Attente d'une nouvelle requête HTTP.
            String header = inputStream.readLine();
            //Si l'attente n'a rien donné.
            if(header == null) {
                //On arrête la connexion en cours.
                stopTcpConnection();
                Thread.currentThread().interrupt();
                Thread.currentThread().stop();
            }

            //Récupération de la commande et des entêtes
            //de la requête HTTP.
            headers.add(header);
            //Tant que la ligne finale vide n'est pas atteinte, on lit les entêtes.
            while(!(header = inputStream.readLine()).isEmpty())  {
                headers.add(header);
            }
        } catch (IOException exception) {
            httpServer.logErrorAndExit("Erreur à la lecture d'une requête HTTP !", exception);
        }

        //Analyse de la requête HTTP.
        lastHttpRequest = new HttpRequest(headers);

        //Log de réception de la requête HTTP.
        httpServer.logDatedMessage(ipHoteClient + " " +
                                   lastHttpRequest.get("Method") + " "  +
                                   lastHttpRequest.get("URI"));
    }

    /**
     * Envoyer une réponse HTTP à la
     * dernière requête HTTP reçue.
     */
    private void respondToHttpRequest() {
        //Construction la réponse HTTP.
        byte[] responseAsBytes = HttpResponse.render(lastHttpRequest);
        //Envoi de la réponse HTTP.
        try {
            outputStream.write(responseAsBytes);
            outputStream.flush();
        } catch (IOException exception) {
            httpServer.logErrorAndExit("Erreur à l'envoi d'une réponse HTTP !", exception);
        }
    }

    /**
     * Terminer l'échange : arrêter la
     * connexion TCP.
     */
    private void stopTcpConnection() {
        try {
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (IOException exception) {
            httpServer.logErrorAndExit("Erreur à l'arrêt de la connexion TCP !", exception);
        }
    }

    /**
     * Savoir si le client web veut conserver
     * la connexion actuelle.
     *
     * @return
     */
    public boolean clientKeepsConnection() {
        return !lastHttpRequest.contains("Connection")//Si la requête est en HTTP 1.0, cet entête n'est pas connu.
               || lastHttpRequest.get("Connection").equals("keep-alive");//Si le client web veut conserver la connexion.
    }
}