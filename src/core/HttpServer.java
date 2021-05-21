package core;

import org.jetbrains.annotations.NotNull;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Properties;

/**
 * Classe pour la gestion du serveur HTTP.
 *
 * @author CHEVRIER Jean-Christophe, HADJ MESSAOUD Yousra, LOUGADI Marième,
 *         étudiants en MASTER 1 MIAGE, à l'université de Lorraine.
 */
public class HttpServer {
    //Nom du fichier de configuration du serveur.
    private final static String CONFIGURATION_FILENAME = "configuration.properties";
    //Nom du fichier des types de contenu.
    private final static String CONTENT_TYPES_FILENAME = "content_types.properties";
    //Nom du fichier des alias des documents du serveur / hôtes.
    private final static String HOSTS_FILENAME = "hosts.properties";
    //Serveur TCP, pour écouter les connexions TCP.
    private ServerSocket tcpServer;
    //Singleton serveur HTTP.
    private static HttpServer singletonHttpServer;
    //Configuration du serveur.
    private Properties configuration;
    //Types de contenu.
    private Properties contentTypes;
    //Alias des documents du serveur / hôtes.
    private Properties hosts;

    /**
     * Créer un objet core.HttpServer.
     */
    private HttpServer() {
        //Chargement de la configuration du serveur.
        loadConfiguration();
        //Chargement des types de contenu.
        loadContentTypes();
        //Chargement des alias des documents du serveur / hôtes.
        loadHosts();
    }

    /**
     * Obtenir le singleton serveur HTTP.
     */
    public static HttpServer getInstance() {
        if(singletonHttpServer == null) {
            singletonHttpServer = new HttpServer();
        }
        return singletonHttpServer;
    }

    /**
     * Charger la configuration du serveur.
     */
    private void loadConfiguration() {
        configuration = new Properties();
        try {
            configuration.load(new FileReader(FileManager.getConfigurationFile(CONFIGURATION_FILENAME)));
        } catch (IOException exception) {
            logErrorAndExit("Le fichier de configuration '"+ CONFIGURATION_FILENAME + "' n'a pas pu être trouvé !", exception);
        }
    }

    /**
     * Obtenir une propriété de configuration
     * du serveur.
     *
     * @param propertyName
     * @return
     */
    public String getConfigurationProperty(@NotNull String propertyName) {
        if(!configuration.containsKey(propertyName)) {
            throw new IllegalArgumentException("Propriété de configuration du serveur introuvable : " + propertyName + " !");
        }
        return configuration.getProperty(propertyName);
    }

    /**
     * Obtenir une propriété de configuration
     * du serveur de type entier.
     *
     * @param propertyName
     * @return
     */
    public int getConfigurationPropertyInteger(@NotNull String propertyName) {
        return Integer.parseInt(getConfigurationProperty(propertyName));
    }

    /**
     * Obtenir une propriété de configuration
     * du serveur de type booléen.
     *
     * @param propertyName
     * @return
     */
    public boolean getConfigurationPropertyBoolean(@NotNull String propertyName) {
        return Boolean.parseBoolean(getConfigurationProperty(propertyName));
    }

    /**
     * Charger les types de contenu.
     */
    private void loadContentTypes() {
        contentTypes = new Properties();
        try {
            contentTypes.load(new FileReader(FileManager.getConfigurationFile(CONTENT_TYPES_FILENAME)));
        } catch (IOException exception) {
            logErrorAndExit("Le fichier des types de contenu '"+ CONTENT_TYPES_FILENAME + "' n'a pas pu être trouvé !", exception);
        }
    }

    /**
     * Obtenir un type de contenu pour une
     * extension de fichier.
     *
     * @param extensionFile
     * @return
     */
    public String getContentType(@NotNull String extensionFile) {
        if(!contentTypes.containsKey(extensionFile)) {
            throw new IllegalArgumentException("Extension de fichier non répertoriée : " + extensionFile + " !");
        }
        return contentTypes.getProperty(extensionFile);
    }

    /**
     * Charger les types de contenu.
     */
    private void loadHosts() {
       hosts = new Properties();
        try {
            hosts.load(new FileReader(FileManager.getConfigurationFile(HOSTS_FILENAME)));
        } catch (IOException exception) {
            logErrorAndExit("Le fichier des types de contenu '"+ CONTENT_TYPES_FILENAME + "' n'a pas pu être trouvé !", exception);
        }
    }

    /**
     * Obtenir l'URI à partir d'un alias de
     * documents du serveur / hôte.
     *
     * @param host
     * @return
     */
    public String getURIForHost(@NotNull String host) {
        if(!hosts.containsKey(host)) {
            throw new IllegalArgumentException("Alias de documents du serveur / hôte : " + host + " !");
        }
        return hosts.getProperty(host);
    }

    /**
     * Savoir si l'hôte demandé est un alias
     * de documents du serveur.
     *
     * @param host
     * @return
     */
   public boolean hostAskedIsAlias(@NotNull String host) {
       return hosts.containsKey(host);
   }

    /**
     * Démarrer le serveur TCP.
     */
    private void startTcpServer() {
        //Récupération de la configuration du serveur.
        String address = getConfigurationProperty("address");
        int port = getConfigurationPropertyInteger("port");
        int countMaxConnections = getConfigurationPropertyInteger("countMaxConnections");

        //Démarrage du serveur TCP.
        try {
            tcpServer = new ServerSocket(port, countMaxConnections, InetAddress.getByName(address));
        } catch (IOException exception) {
            logErrorAndExit("Le serveur des connexions TCP interne n'a pas pu être démarré !", exception);
        }

        //Log de démarrage du serveur.
        logMessage("Faites CTRL + C pour arrêter le serveur.\n");
        logDatedMessage("Serveur démarré, et écoute à l'adresse " + address + ", sur le port " + port + ".");
    }

    /**
     * Démarrer l'handler sur les connexions HTTP.
     */
    private void startHandlerTcpConnection() {
        while(true) {
            //On écoute les connexions TCP des clients web.
            Socket tcpConnection = null;
            try {
                tcpConnection = tcpServer.accept();
            } catch (IOException exception) {
                logErrorAndExit("Erreur à la réception d'une connexion TCP !", exception);
            }
            //Si on a reçu une nouvelle connexion TCP,
            //on la traite dans un nouveau thread.
            handleTcpConnectionInNewThread(tcpConnection);
        }
    }

    /**
     * Attraper une connexion TCP, puis gérer les trames HTTP,
     * qui y sont échangées dans un nouveau thread.
     *
     * @param
     */
    private static void handleTcpConnectionInNewThread(@NotNull Socket tcpConnection) {
        //On lance dans un nouveau thread la gestion des trames échangées sur la nouvelle connexion.
        Thread threadNewHttpConnection = new Thread(() -> HttpConnection.handle(tcpConnection));
        threadNewHttpConnection.start();
    }

    /**
     * Démarrer le serveur, et l'handler sur les connexions TCP.
     */
    public void start() {
        //Démarrage du serveur TCP.
        startTcpServer();
        //Démarrage de l'écoute du serveur TCP.
        startHandlerTcpConnection();
    }

    /**
     * Logger un message.
     */
    public void logMessage(@NotNull String message) {
        System.out.println(message);
    }

    /**
     * Logger un message daté.
     */
    public void logDatedMessage(@NotNull String message) {
        logMessage("[" + new Date().toLocaleString() + "] " + message);
    }

    /**
     * Logger un message d'erreur.
     */
    public void logErrorAndExit(@NotNull String message, @NotNull Exception exception) {
        System.err.println(message);
        exception.printStackTrace();
        System.exit(1);
    }
}