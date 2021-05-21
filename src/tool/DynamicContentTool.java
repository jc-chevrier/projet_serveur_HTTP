package tool;

import core.FileManager;
import org.jetbrains.annotations.NotNull;

/**
 * Classe proposant des outils, pour implémenter
 * les contenus dynamiques obtenus par l'éxecution
 * de programmes, tel que les programme .php.
 *
 * @author CHEVRIER Jean-Christophe, HADJ MESSAOUD Yousra, LOUGADI Marième,
 *         étudiants en MASTER 1 MIAGE, à l'université de Lorraine.
 */
public class DynamicContentTool {
    /**
     * Contruire un contenu dynamique
     * à partir d'un programme .php.
     *
     * @param documentFilename
     * @return
     */
    public static String buildContentFromPHP(@NotNull String documentFilename) {
        //Récupération du chemin
        //absolu vers le fichier.
        String documentAbsolutePath = FileManager.getDocumentFile(documentFilename).getAbsolutePath();

        //Exécution de la commande, et
        //récupération du résultat.
        String[] command = new String[]{"php", documentAbsolutePath};
        String content = ProcessTool.executeAndBindOutput(command);

        return content;
    }
}