package core;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe pour l'analyse des requêtes HTTP.
 *
 * Format d'une requête HTTP :
 * [Method] [URI] HTTP/[Version]        (ligne de commande)
 * [nomEntête]: [valeurEntête]          (n lignes d'entete)
 *                                      (ligne vide)
 * Voir : https://fr.wikipedia.org/wiki/Hypertext_Transfer_Protocol
 *
 * @author CHEVRIER Jean-Christophe, HADJ MESSAOUD Yousra, LOUGADI Marième,
 *         étudiants en MASTER 1 MIAGE, à l'université de Lorraine.
 */
public class HttpRequest {
    //Singleton serveur HTTP.
    private final static HttpServer httpServer = HttpServer.getInstance();
    //Table des données (commande + entêtes) de la requête HTTP.
    private Map<String, String> datas;

    /**
     * Créer un objet core.HttpRequest
     * à partir des données d'une requête
     * HTTP passés en paramètre.
     *
     * Une donnée de la requête peut être :
     * - un élément de la commande ;
     * - un entête.
     *
     * @param datas
     */
    public HttpRequest(@NotNull List<String> datas) {
        //Analyse de la commande et des entêtes
        //de la requête HTTP.
        parse(datas);
    }

    /**
     * Analyser et convertir les données
     * de la requête HTTP.
     *
     * Une donnée de la requête peut être :
     * - un élément de la commande ;
     * - un entête.
     *
     * @param datas
     */
    private void parse(@NotNull List<String> datas) {
        this.datas = new HashMap<String, String>();
        parseCommand(datas.remove(0));
        parseHeaders(datas);
        updateURIIfHostAlias();
    }

    /**
     * Analyser et convertir la commande
     * de la requête HTTP.
     *
     * @param command
     */
    private void parseCommand(@NotNull String command) {
        String[] commandSplit = command.split(" ");
        datas.put("Method", commandSplit[0]);
        datas.put("URI", commandSplit[1]);
        datas.put("Version", commandSplit[2].replace("HTTP/", ""));
    }

    /**
     * Analyser et convertir les entêtes
     * de la requête HTTP.
     *
     * @param headers
     */
    private void parseHeaders(@NotNull List<String> headers) {
        for(String header : headers) {
            parseHeader(header);
        }
    }

    /**
     * Analyser et convertir un entête
     * de la requête HTTP.
     *
     * @param header
     */
    private void parseHeader(@NotNull String header) {
        String[] headerSplit = header.split(":");
        datas.put(headerSplit[0], headerSplit[1].trim());
    }

    /**
     * Mettre à jour l'URI si l'hôte demandé
     * est un alias des documents du serveur.
     */
    private void updateURIIfHostAlias() {
        //Hôte demandé.
        String host = get("Host");
        //URI demandé.
        String URI = get("URI");
        if(httpServer.hostAskedIsAlias(host)) {
            String rootURI = httpServer.getURIForHost(host);
            datas.put("URI", "/" + rootURI + URI);
        }
    }

    /**
     * Savoir si une donnée de la requête
     * HTTP est connue.
     *
     * Une donnée de la requête peut être :
     * - un élément de la commande ;
     * - un entête.
     *
     * @param nameData
     * @return
     */
    public boolean contains(@NotNull String nameData) {
        return datas.containsKey(nameData);
    }

    /**
     * Obtenir la valeur d'une donnée de la
     * requête HTTP.
     *
     * Une donnée de la requête peut être :
     * - un élément de la commande ;
     * - un entête.
     *
     * @param nameData
     * @return
     */
    public String get(@NotNull String nameData) {
        if (!contains(nameData)) {
            throw new IllegalArgumentException("Donnée de la requête HTTP demandée inconnue : " + nameData + "!");
        }
        return datas.get(nameData);
    }
}