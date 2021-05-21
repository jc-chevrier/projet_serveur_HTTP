package tool;

import core.FileManager;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe proposant des outils, pour implémenter
 * les server side includes #include et #exec.
 *
 * Voir : https://fr.wikipedia.org/wiki/Server_Side_Includes
 *
 * @author CHEVRIER Jean-Christophe, HADJ MESSAOUD Yousra, LOUGADI Marième,
 *         étudiants en MASTER 1 MIAGE, à l'université de Lorraine.
 */
public class ServerSideIncludesTool {
    //Expression régulières des server sides includes.
    public final static String REGEX_INCLUDE_START = "<!--.*#include *file *= *\"";
    public final static String REGEX_INCLUDE_END = "\".*-->";
    public final static String REGEX_INCLUDE = "<!--.*#include *file *= *\".*\".*-->";
    public final static String REGEX_EXEC_START = "<!--.*#exec *cmd *= *\"";
    public final static String REGEX_EXEC_END = "\".*-->";
    public final static String REGEX_EXEC = "<!--.*#exec *cmd *= *\".*\".*-->";

    /**
     * Constuire le contenu d'un document html
     * du serveur en prenant en compte les
     * server sides includes #include et #exec
     * utilisés s'il y en a.
     *
     * @param documentFilename
     * @return
     */
    public static String buildContent(@NotNull String documentFilename) {
        //Chargement du contenu du document.
        String documentContent = FileManager.getContentDocumentFileAsString(documentFilename);
        //Récupération du chemin relatif
        //du répertoire parent du document.
        String parentDirectoryPath = FileManager.getParentDirectoryPath(documentFilename);
        //Prise en compte des server sides
        //includes de type #include.
        documentContent = buildIncludes(parentDirectoryPath, documentContent);
        //Prise en compte des server sides
        //includes de type #exec.
        documentContent = buildExecs(parentDirectoryPath, documentContent);
        return documentContent;
    }

    /**
     * Gérer les server sides includes de type
     * #include d'un document du serveur.
     *
     * @param parentDirectoryPath
     * @param documentContent
     * @return
     */
    private static String buildIncludes(@NotNull String parentDirectoryPath, @NotNull String documentContent) {
        //Recherche des server sides includes
        //de type #include.
        Pattern pattern = Pattern.compile(REGEX_INCLUDE);
        Matcher matcher = pattern.matcher(documentContent);

        //Tant que des server sides includes
        //#include sont trouvés.
        while (matcher.find()) {
            String htmlComment = matcher.group(0);
            //Récupération du chmein relatif
            //du document à inclure.
            String documentIncludedFilename = htmlComment.replaceFirst(REGEX_INCLUDE_START, "")
                                                         .replaceFirst(REGEX_INCLUDE_END, "");
            documentIncludedFilename = parentDirectoryPath + File.separator + documentIncludedFilename;

            //Chargement du contenu du document
            //à inclure.
            String documentIncludedContent = buildInclude(documentIncludedFilename);

            //Remplacement par le contenu obtenu.
            documentContent = documentContent.replace(htmlComment, documentIncludedContent);

            //Recherche des server sides includes
            //de type #include restantes.
            matcher = pattern.matcher(documentContent);
        }

        return documentContent;
    }

    /**
     * Gérer un server sides
     * includes de type #include.
     *
     * @param documentIncludedFilename
     * @return
     */
    private static String buildInclude(@NotNull String documentIncludedFilename) {
        return FileManager.getContentDocumentFileAsString(documentIncludedFilename);
    }


    /**
     * Gérer les server sides includes de
     * type #exed d'un document du serveur.
     *
     * @param parentDirectoryPath
     * @param documentContent
     * @return
     */
    private static String buildExecs(@NotNull String parentDirectoryPath, @NotNull String documentContent) {
        //Récupération du chemin absolu du répertoire
        //parent du document du serveur.
        String parentDirectoryAbsolutePath = FileManager.getDocumentFile(parentDirectoryPath).getAbsolutePath();

        //Recherche des server sides includes
        //de type #exec.
        Pattern pattern = Pattern.compile(REGEX_EXEC);
        Matcher matcher = pattern.matcher(documentContent);

        //Tant que des des server sides includes
        //#exec sont trouvés.
        while (matcher.find()) {
            //Récupération de la commande.
            String htmlComment = matcher.group(0);
            String command = htmlComment.replaceFirst(REGEX_EXEC_START, "")
                                        .replaceFirst(REGEX_EXEC_END, "");

            //Exécution de la commande, et
            //récupération du résultat.
            String content = buildExec(parentDirectoryAbsolutePath, command);

            //Remplacement par le résultat de
            //la commande.
            documentContent = documentContent.replace(htmlComment, content);

            //Recherche des server sides includes
            //de type #exec restantes.
            matcher = pattern.matcher(documentContent);
        }

        return documentContent;
    }

     /**
      * Gérer un server sides
      * includes de type #exec.
     *
     * @param directoryAbsolutePath
     * @param command
     * @return
     */
    private static String buildExec(@NotNull String directoryAbsolutePath, @NotNull String command) {
        return ProcessTool.executeTerminalCommand(directoryAbsolutePath, command);
    }
}