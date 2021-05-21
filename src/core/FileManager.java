package core;

import org.jetbrains.annotations.NotNull;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Classe pour la gestion des documents du serveur.
 *
 * @author CHEVRIER Jean-Christophe, HADJ MESSAOUD Yousra, LOUGADI Marième,
 *         étudiants en MASTER 1 MIAGE, à l'université de Lorraine.
 */
public class FileManager {
    //Singleton serveur HTTP.
    private final static HttpServer httpServer = HttpServer.getInstance();
    //Chemin vers le répértoire des fichiers de configuration du serveur.
    public final static String CONFIGURATION_DIRECTORY = "configuration";
    //Chemin vers le répertoire des documents du serveur: ses sites web.
    public final static String DOCUMENT_DIRECTORY = HttpServer.getInstance().getConfigurationProperty("rootPathDocuments");

    /**
     * Charger le répertoire des fichiers
     * de configuration du serveur.
     *
     * @return
     */
    public static File getConfigurationDirectory() {
        return new File(CONFIGURATION_DIRECTORY);
    }

    /**
     * Charger un fichier de configuration
     * du serveur.
     *
     * @param configurationFilename
     * @return
     */
    public static File getConfigurationFile(@NotNull String configurationFilename) {
        return new File(CONFIGURATION_DIRECTORY + File.separator + configurationFilename);
    }

    /**
     * Vérifier si un fichier de configuration
     * du serveur existe.
     *
     * @param configurationFilename
     * @return
     */
    public static boolean configurationFileExists(@NotNull String configurationFilename) {
        File document = getConfigurationFile(configurationFilename);
        return document.isFile() && document.exists();
    }

    /**
     * Charger le répertoire des documents
     * du serveur.
     *
     * @return
     */
    public static File getDocumentDirectory() {
        return new File(DOCUMENT_DIRECTORY);
    }

    /**
     * Charger un document du serveur.
     *
     * @param documentFilename
     * @return
     */
    public static File getDocumentFile(@NotNull String documentFilename) {
        return new File(DOCUMENT_DIRECTORY + File.separator + documentFilename);
    }

    /**
     * Vérifier si un document du serveur existe.
     *
     * @param documentFilename
     * @return
     */
    public static boolean documentFileExists(@NotNull String documentFilename) {
        File document = getDocumentFile(documentFilename);
        return document.isFile() && document.exists();
    }

    /**
     * Charger le contenu d'un document du serveur
     * sous la forme d'un tableau d'octets.
     *
     * @param documentFilename
     * @return
     */
    public static byte[] getContentDocumentFileAsBytes(@NotNull String documentFilename) {
        byte[] content = null;
        try {
            content = Files.readAllBytes(getDocumentFile(documentFilename).toPath());
        } catch (IOException exception) {
            httpServer.logErrorAndExit("Erreur lors de la lecture du document du serveur : " + documentFilename + " !", exception);
        }
        return content;
    }

    /**
     * Charger le contenu d'un document du serveur
     * sous la forme d'une chaine de caractères.
     *
     * @param documentFilename
     * @return
     */
    public static String getContentDocumentFileAsString(@NotNull String documentFilename) {
        return new String(getContentDocumentFileAsBytes(documentFilename));
    }

    /**
     * Vérifier si un document du serveur
     * est protégé, car contenu dans un
     * répertoire de documents protégé.
     *
     * @param documentFilename
     * @return
     */
    public static boolean documentFileIsInProtectedDirectory(@NotNull String documentFilename) {
        boolean isInProtectedDirectory = false;
        File documentParentDirectory = getDocumentFile(documentFilename).getParentFile();

        //Remontée dans les repertoires parents
        //du document.Tant que racine des documents pas atteinte.
        while(!documentParentDirectory.getName().equals(DOCUMENT_DIRECTORY)) {
            //Si le répertoire contient un fichier
            //d'authentifications, alors c'est que
            //le document est protégé.
            if(Arrays.asList(documentParentDirectory.list()).contains(".htpasswd")) {
                isInProtectedDirectory = true;
                break;
            //Sinon, on remonte au répertoire parent.
            } else {
                documentParentDirectory = documentParentDirectory.getParentFile();
            }
        }

        return isInProtectedDirectory;
    }

    /**
     * Obtenir le chemin relatif du fichier
     * d'authentification d'un répertoire protégé
     * d'un document du serveur.
     *
     * @param documentFilename
     * @return
     */
    public static String getAuthenticationFileNameFor(@NotNull String documentFilename) {
        boolean authenticationFound = false;
        String authenticationFileName = "";
        File documentFile = getDocumentFile(documentFilename);
        File documentParentDirectory = documentFile.getParentFile();

        //Remontée dans les repertoires parents du document.
        //Tant que racine des documents pas atteinte.
        while(!documentParentDirectory.getName().equals(DOCUMENT_DIRECTORY)) {
            //Si fichier d'autentifications trouvé.
            if(Arrays.asList(documentParentDirectory.list()).contains(".htpasswd")
               || authenticationFound) {
                if(!authenticationFound) {
                    authenticationFound = true;
                }
                //Reconstitution du chemin relatif
                //du fichier d'autentifications.
                authenticationFileName = documentParentDirectory.getName() + File.separator + authenticationFileName;
            }
            //Remontée au répertoire parent.
            documentParentDirectory = documentParentDirectory.getParentFile();
        }

        if(authenticationFound) {
            authenticationFileName += ".htpasswd";
        } else {
            authenticationFileName = null;
        }

        return authenticationFileName;
    }

    /**
     * Obtenir le contenu du fichier d'authentification
     * pour le répertoire protégé d'un document du serveur.
     *
     * @param documentFilename
     * @return
     */
    public static String getContentAuthenticationFileFor(@NotNull String documentFilename) {
        return getContentDocumentFileAsString(getAuthenticationFileNameFor(documentFilename));
    }

    /**
     * Obtenir le chemin relatif au répertoire
     * des documents du serveur, pour le répertoire
     * parent d'un document du serveur.
     *
     * @param documentFilemane
     * @return
     */
    public static String getParentDirectoryPath(@NotNull String documentFilemane) {
        List<String> split = new ArrayList<String>(Arrays.asList(documentFilemane.split("[/\\\\]")));
        split.remove(split.size() - 1);
        String parentDirectoryPath = String.join(File.separator, split);
        return parentDirectoryPath;
    }

    /**
     * Obtenir l'extention d'un document du
     * serveur.
     *
     * @param documentFilename
     */
    public static String getDocumentFileExtension(@NotNull String documentFilename) {
        String[] documentFilenameSplit = documentFilename.split("\\.");
        String extension = "." +  documentFilenameSplit[documentFilenameSplit.length - 1].toLowerCase();
        return  extension;
    }
}