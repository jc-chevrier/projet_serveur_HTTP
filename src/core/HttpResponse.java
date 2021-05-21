package core;

import org.jetbrains.annotations.NotNull;
import tool.ArrayTool;
import tool.DynamicContentTool;
import tool.ServerSideIncludesTool;
import tool.TreePageTool;

import java.io.File;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe pour la construction des réponses HTTP.
 *
 * Format d'une réponse HTTP :
 * HTTP/[Version] [codeStatut] [messageStatut]         (ligne d'état)
 * [nomEntête]: [valeurEntête]                         (n lignes d'entete)
 *                                                     (ligne vide)
 * [corps]                                             (n lignes de corps de réponse)
 * Voir : https://fr.wikipedia.org/wiki/Hypertext_Transfer_Protocol
 *
 * @author CHEVRIER Jean-Christophe, HADJ MESSAOUD Yousra, LOUGADI Marième,
 *         étudiants en MASTER 1 MIAGE, à l'université de Lorraine.
 */
public class HttpResponse {
    //Singleton serveur HTTP.
    private final static HttpServer httpServer = HttpServer.getInstance();
    //Requête HTTP associée, à laquelle la
    //réponse HTTP répond.
    private HttpRequest httpRequest;
    //Table des données de la requête HTTP
    //(élement de la ligne d'état, les
    //entêtes de réponse, le corps de la
    //réponse).
    private Map<String, Object> datas;

    /**
     * Créer un objet core.HttpResponse
     * à partir d'une requête HTTP associée.
     *
     * @param httpRequest
     */
    public HttpResponse(@NotNull HttpRequest httpRequest) {
        //Sauvegarde de la réponse HTTP associée.
        this.httpRequest = httpRequest;
        //Constuction de la réponse HTTP.
        build();
    }

    /**
     * Constuire la réponse HTTP.
     */
    public void build() {
        this.datas = new HashMap<String, Object>();
        //Construction de la version HTTP.
        buildVersion();
        //Construction des données liées au contenu /
        //corps de la réponse.
        buildDatasContent();
        //Construction de l'entête de connexion.
        buildConnectionHeader();
    }

    /**
     * Construire la version HTTP de la
     * ligne d'état.
     *
     * La réponse HTTP a la même version du
     * protocole HTTP que celle de la requête
     * HTTP.
     */
    public void buildVersion() {
        String version = httpRequest.get("Version");
        set("Version", version);
    }

    /**
     * Construire le corps de la réponse
     * et les autres données associées,
     * si l'URI demandé correspond bien
     * à un document existant sur le serveur.
     */
    public void buildContentForFoundURI() {
        //URI demandé dans la requête HTTP.
        String URI = httpRequest.get("URI");

        //Eléments de la réponse HTTP
        //liés à son contenu.
        int codeStatus = 200;
        String messageStatus = "OK";
        String contentType = getContentTypeForURI();
        //Récupération de l'extension du
        //document du serveur demandé.
        String extension = FileManager.getDocumentFileExtension(URI);
        Object content;
        switch (extension) {
            //Si le document demandé est un document html.
            case ".html" :
                //On charge son contenu, et on prend en
                //compte les server sides includes qu'il
                //utilise.
                content = ServerSideIncludesTool.buildContent(URI);
                break;

            //Si le document demandé est un programme
            //.php produisant un contenu dynamique.
            case ".php" :
                //On charge le contenu dynamique en exécutant
                //le programme .php.
                content = DynamicContentTool.buildContentFromPHP(URI);
                break;

            default :
                //On charge le contenu du document en octets.
                content = FileManager.getContentDocumentFileAsBytes(URI);
        }
        int contentLength;
        //Si le contenu a été chargé en octets.
        if(content instanceof byte[]) {
            contentLength = ((byte[]) content).length;
        //Sinon.
        } else {
            contentLength = ((String) content).getBytes().length;
        }

        //Ajout des données à la réponse HTTP.
        set("Code-Status", codeStatus);
        set("Message-Status", messageStatus);
        set("Content-Type", contentType);
        set("Content-Length", contentLength);
        set("Content", content);
    }

    /**
     * Savoir si l'URI demandé est un URI
     * pour le système de pages affichant
     * l'arborescence des documents du site.
     *
     * @return
     */
    public boolean treeURIAsked()  {
        //URI demandé dans la requête HTTP.
        String URI = httpRequest.get("URI");

        //Configuration du serveur.
        //Paramètres du système d'arborescence du serveur.
        String treeDocumentsURI = httpServer.getConfigurationProperty("treeDocumentsURI");
        boolean treeDocumentsEnable = httpServer.getConfigurationPropertyBoolean("treeDocumentsEnable");

        //Si l'URI demandé est un URI du système
        //d'arborescence, et si l'accès au système
        //d'arborescence est activé / autorisé.
        return URI.startsWith(treeDocumentsURI) && treeDocumentsEnable;
    }

    /**
     * Construire le corps de la réponse
     * et les autres données associées,
     * si l'URI demandé correspond à un URI
     * du système de pages affichant
     * l'arborescence du site.
     */
    public void buildContentForTreeURI() {
        //URI demandé dans la requête HTTP.
        String URI = httpRequest.get("URI");

        //Configuration du serveur.
        //Paramètres du système d'arborescence du serveur.
        String treeDocumentsURI = httpServer.getConfigurationProperty("treeDocumentsURI");

        //Données de la réponse HTTP
        //liées à son contenu.
        int codeStatus = 200;
        String messageStatus = "OK";
        String contentType = "text/html";
        String documentsDirectory = URI.replaceFirst(treeDocumentsURI, "");
        String content = getTreePage(documentsDirectory);
        int contentLength = content.getBytes().length;

        //Ajout des données à la réponse HTTP.
        set("Code-Status", codeStatus);
        set("Message-Status", messageStatus);
        set("Content-Type", contentType);
        set("Content-Length", contentLength);
        set("Content", content);
    }

    /**
     * Construire le corps de la réponse
     * et les autres données associées,
     * si l'URI demandé ne correspond
     * à aucun document existant sur le
     * serveur.
     */
    public void buildContentForNotFoundURI() {
        //URI demandé dans la requête HTTP.
        String URI = httpRequest.get("URI");

        //Données de la réponse HTTP
        //liées à son contenu.
        int codeStatus = 404;
        String messageStatus = "Not Found";
        String contentType = "text/html";
        String errorMessage = "Le document \"" +  URI + "\" est introuvable sur le serveur !";
        String content = getErrorPage(codeStatus, errorMessage);
        int contentLength = content.getBytes().length;

        //Ajout des données à la réponse HTTP.
        set("Code-Status", codeStatus);
        set("Message-Status", messageStatus);
        set("Content-Type", contentType);
        set("Content-Length", contentLength);
        set("Content", content);
    }

    /**
     * Savoir si une authentification
     * a été faite, pour un accès à un
     * document du serveur dans un
     * répertoire protégé.
     *
     * @return
     */
    public boolean authenticationHasBeenDone() {
        return httpRequest.contains("Authorization");
    }

    /**
     * Construire le corps de la réponse
     * et les autres données associées,
     * si l'URI demandé correspond à
     * document existant sur le serveur,
     * qui est dans un répertoire protégé.
     */
    public void buildContentForProtectedURI() {
        //URI demandé dans la requête HTTP.
        String URI = httpRequest.get("URI");

        //Données de la réponse HTTP
        //liées à son contenu.
        int codeStatus = 401;
        String messageStatus = "Unauthorized";
        String contentType = "text/html";
        String errorMessage = "Le document \"" +  URI + "\" est protégé sur le serveur !";
        String content = getErrorPage(codeStatus, errorMessage);
        int contentLength = content.getBytes().length;
        String WWWAuthenticate = "Basic realm=\"Access to the staging site\"";

        //Ajout des données à la réponse HTTP.
        set("Code-Status", codeStatus);
        set("Message-Status", messageStatus);
        set("Content-Type", contentType);
        set("Content-Length", contentLength);
        set("Content", content);
        set("WWW-Authenticate",WWWAuthenticate);
    }

    /**
     * Savoir si l'authentification qui a
     * été faite, pour un accès à un document
     * du serveur dans un répertoire protégé
     * est correcte.
     */
    public boolean authenticationDoneCorrect() {
        //URI demandé dans la requête HTTP.
        String URI = httpRequest.get("URI");

        //Authentification faite.
        Base64.Decoder decoder = Base64.getDecoder();
        String authentication = httpRequest.get("Authorization").split(" ")[1];
        String authenticationObtained = new String(decoder.decode(authentication));

        //Authentification attendue.
        String lineSeparator = System.getProperty("line.separator");
        String[] authenticationExpected = FileManager.getContentAuthenticationFileFor(URI).split(lineSeparator);

        //Si l'authentification faite est correcte.
        return Arrays.asList(authenticationExpected).contains(authenticationObtained);
    }

    /**
     * Construire le corps de la réponse
     * et les autres données associées,
     * si l'URI demandé correspond à
     * document existant sur le serveur,
     * qui est dans un répertoire protégé,
     * et que l'authentification a échouée.
     */
    public void buildContentForBadAuthentication() {
        //URI demandé dans la requête HTTP.
        String URI = httpRequest.get("URI");

        //Données de la réponse HTTP
        //liées à son contenu.
        int codeStatus = 403;
        String messageStatus = "Forbidden";
        String contentType = "text/html";
        String errorMessage = "Accès refusé à ce document protégé : \"" +  URI + "\" !";
        String content = getErrorPage(codeStatus, errorMessage);
        int contentLength = content.getBytes().length;

        //Ajout des données à la réponse HTTP.
        set("Code-Status", codeStatus);
        set("Message-Status", messageStatus);
        set("Content-Type", contentType);
        set("Content-Length", contentLength);
        set("Content", content);
    }

    /**
     * Construire les données de la réponse HTTP
     * dépendant du contenu / corps de la réponse.
     */
    public void buildDatasContent() {
        //URI demandé dans la requête HTTP.
        String URI = httpRequest.get("URI");

        //Si l'URI demandé est une page du
        //système affichant l'arborescence
        //du serveur.
        if(treeURIAsked()) {
            buildContentForTreeURI();
       //Sinon.
        } else {
            //Si l'URI correspond bien à un document
            //du serveur.
            if(FileManager.documentFileExists(URI)) {
                //Si l'URI demandé correspond à un document
                //du serveur dans un réperoire protégé.
                if(FileManager.documentFileIsInProtectedDirectory(URI)) {
                    //Si l'authentification a été faite,
                    //alors on la vérifie.
                    if(authenticationHasBeenDone()) {
                        //Si l'authentification faite est correcte
                        //alors accès au document du serveur donné.
                        if(authenticationDoneCorrect()) {
                            buildContentForFoundURI();
                            //Sinon, mauvaise authentification faite,
                            //et page d'erreur.
                        } else {
                            buildContentForBadAuthentication();
                        }
                        //Sinon, l'authentification doit être
                        //demandé.
                    } else {
                        buildContentForProtectedURI();
                    }
                    //Sinon, pas d'authenitification demadné,
                    //et donc accès direct au document demandé.
                } else {
                    buildContentForFoundURI();
                }
                //Sinon, le document cherché sur le serveur
                //est introuvable, alors on envoie une
                //page d'erreur.
            } else {
                buildContentForNotFoundURI();
            }
        }
    }

    /**
     * Construire l'entête connexion (conserver
     * ou arrêter la connexion HTTP avec le client).
     */
    public void buildConnectionHeader() {
        if(httpRequest.contains("Connection")) {
            String connection = httpRequest.get("Connection");
            set("Connection", connection);
        }
    }

    /**
     * Obtenir le type de contenu à ârtir
     * de l'URI.
     *
     * @return
     */
    public String getContentTypeForURI() {
        String URI = httpRequest.get("URI");
        String extension = FileManager.getDocumentFileExtension(URI);
        String contentType = httpServer.getContentType(extension);
        return contentType;
    }

    /**
     * Produire la réponse HTTP en octets.
     *
     * @return
     */
    public byte[] render() {
        String responseAsString;
        byte[] responseAsBytes;

        //Séparateur de lignes du système d'exploitation du serveur
        //(windows, mac, linux, etc).
        String lineSeparator = System.getProperty("line.separator");

        //Ligne d'état et entêtes de base.
        responseAsString = "HTTP/" + get("Version") + " " + get("Code-Status") + " " + get("Message-Status") + lineSeparator +
                           "Content-Type: " + get("Content-Type") + ";charset=UTF-8" + lineSeparator +
                           "Content-Length: " + get("Content-Length") + lineSeparator;

        //Entêtes facultatifs.
        //Cet entête n'existe qu'à partir de HTTP 1.1.
        if(contains("Connection")) {
            responseAsString += "Connection: " + get("Connection") + lineSeparator;
        }
        //Si la ressource demandée est dans un répertoire protégé.
        if(contains("WWW-Authenticate")) {
            responseAsString += "WWW-Authenticate: " + get("WWW-Authenticate") + lineSeparator;
        }

        //Séparation avec le contenu / corps du message :
        //une ligne vide.
        responseAsString += lineSeparator;

        //Contenu / corps du message.
        Object content = get("Content");
        //Si le contenu a été chargé en octets.
        if(content instanceof byte[]) {
            responseAsBytes = responseAsString.getBytes();
            responseAsBytes = ArrayTool.concatenateBytesArrays(responseAsBytes, ((byte[]) content));
        //Sinon.
        } else {
            responseAsString += ((String) content);
            responseAsBytes = responseAsString.getBytes();
        }

        return responseAsBytes;
    }

    /**
     * Construire la réponse HTTP pour une requête HTTP,
     * et l'obtenir en octets.
     *
     * @return
     */
    public static byte[] render(@NotNull HttpRequest httpRequest) {
        //Construction de la réponse.
        HttpResponse httpResponse = new HttpResponse(httpRequest);
        //Production de la réponse en octets.
        byte[] responseAsBytes = httpResponse.render();
        return responseAsBytes;
    }

    /**
     * Ajouter / modifier une donnée de la réponse HTTP.
     *
     * @param nameData
     * @param data
     */
    public void set(@NotNull String nameData, @NotNull Object data) {
        datas.put(nameData, data);
    }


    /**
     * Savoir si une donnée de la réponse
     * HTTP est connue.
     *
     * Un donnée de la réponse HTTP peut être :
     * - un élément de la ligne d'état ;
     * - un entête de retour ;
     * - le corps de la réponse.
     *
     * @param nameData
     * @return
     */
    public boolean contains(@NotNull String nameData) {
        return datas.containsKey(nameData);
    }

    /**
     * Obtenir une donnée de la réponse HTTP.
     *
     * Une donnée de la réponse HTTP peut être :
     * - un élément de la ligne d'état ;
     * - un entête de retour ;
     * - le corps de la réponse.
     *
     * @param nameData
     * @return
     */
    public Object get(@NotNull String nameData) {
        if (!contains(nameData)) {
            throw new IllegalArgumentException("Elément de la réponse HTTP demandé inconnu : " + nameData + "!");
        }
        return datas.get(nameData);
    }

    /**
     * Obtenir une page d'erreur en précisant
     * le code d'erreur et le message.
     *
     * @param errorCode
     * @param errorMessage
     * @return
     */
    private String getErrorPage(@NotNull Integer errorCode, @NotNull String errorMessage) {
        return FileManager.getContentDocumentFileAsString(".server/error/html/index.html")
                          .replace("[PARAM=errorCode=PARAM]", errorCode.toString())
                          .replace("[PARAM=errorMessage=PARAM]", errorMessage);
    }

    /**
     * Obtenir une page décrivant le contenu
     * d'un répertoire de documents du sereur :
     * une page web du système affichant
     * l'arborescence des documents du
     * serveur.
     *
     * @param pathDocumentsDirectory
     * @return
     */
    private String getTreePage(@NotNull String pathDocumentsDirectory) {
        return FileManager.getContentDocumentFileAsString(".server/tree/html/index.html")
                          .replace("[PARAM=treePage=PARAM]", TreePageTool.buildDocumentsTreePage(pathDocumentsDirectory));
    }
}