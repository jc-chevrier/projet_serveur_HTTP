package tool;

import core.FileManager;
import core.HttpServer;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.util.Arrays;

/**
 * Classe pour la construction des pages web
 * d'arborescence du site.
 *
 * @author CHEVRIER Jean-Christophe, HADJ MESSAOUD Yousra, LOUGADI Marième,
 *         étudiants en MASTER 1 MIAGE, à l'université de Lorraine.
 */
public class TreePageTool {
    /**
     * Construire une page listant les sous-
     * répertoires et les sous-fichiers d'un
     * répertoire de documents du serveur.
     *
     * @param pathDocumentsDirectory
     * @return
     */
    public static String buildDocumentsTreePage(@NotNull String pathDocumentsDirectory) {
        File documentsDirectory = FileManager.getDocumentFile(pathDocumentsDirectory);

        String treeDocumentsURI = HttpServer.getInstance().getConfigurationProperty("treeDocumentsURI");

        String treePage = "<section class=\"documents-directory-inner-content\"> " +
                                "<header class=\"documents-directory\">" +
                                        (pathDocumentsDirectory.isEmpty() ? "/" : pathDocumentsDirectory)  +
                                "</header>" +
                                "<div class=\"documents-sub-directory-container\">";

        if (!pathDocumentsDirectory.isEmpty()) {
            String pathPreviousDocumentsDirectory = pathDocumentsDirectory
                                                    .replace("/" + Arrays.stream(pathDocumentsDirectory.split("/"))
                                                             .reduce((accumulator, element) -> element).get(),
                                                             "");
            treePage += "<a class=\"documents-sub-directory\" " +
                        "href=\"" + treeDocumentsURI + pathPreviousDocumentsDirectory + "\">" +
                            ".." +
                        "</a>";
        }

        for (File documentsSubDirectory : documentsDirectory.listFiles()) {
            String pathDocumentsSubDirectory = documentsSubDirectory.getName();
            if(documentsSubDirectory.isFile()) {
                treePage += "<span class=\"documents-sub-directory\">" +
                                pathDocumentsSubDirectory +
                            "</span>";
            } else {
                treePage += "<a class=\"documents-sub-directory\" " +
                            "href=\"" + treeDocumentsURI  + pathDocumentsDirectory + "/" + pathDocumentsSubDirectory + "\">" +
                                pathDocumentsSubDirectory +
                            "</a>";
            }
        }

        treePage +=     "</div>" +
                    "</section>";

        return treePage;
    }
}