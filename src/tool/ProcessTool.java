package tool;

import core.HttpServer;
import org.jetbrains.annotations.NotNull;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe ppur exécuter une commande
 * externe à java.
 *
 * @author CHEVRIER Jean-Christophe, HADJ MESSAOUD Yousra, LOUGADI Marième,
 *         étudiants en MASTER 1 MIAGE, à l'université de Lorraine.
 */
public class ProcessTool {
    /**
     * Exécuter une commande du terminal
     * de l'OS du serveur, en précisant
     * le répertoire de l'exécution et la
     * commande.
     *
     * @param directoryAbsolutePath
     * @param command
     * @return
     */
    public static String executeTerminalCommand(@NotNull String directoryAbsolutePath, @NotNull String command) {
        //Ajout de l'appel au terminal
        //associé à l'OS, pour la commande.
        String[] completeCommand;
        String os = System.getProperty("os.name").toLowerCase();
        if(os.contains("windows")) {
            completeCommand = new String[]{"cmd", "/c", "cd " + directoryAbsolutePath + " && " + command};
        } else {
            completeCommand = new String[]{"/bin/bash", "-c",  "cd " + directoryAbsolutePath + " && " + command};;
        }

        //Exécution de la commande, et récupération
        //du contenu de la sortie "remplie" (STDERR ou STDOUT)
        //de la commande.
        String content = executeAndBindOutput(completeCommand);

        return content;
    }

    /**
     * Exécuter une commande et récupérer
     * le contenu du flux sortie
     * "rempli" uniquement (STDERR ou STDOUT).
     *
     * Si la commande réussit, c'est la
     * sortie standard STDOUT qui est
     * retournée.
     *
     * Sinon si la commande échoue, c'est
     * la sortie d'erreur STDERR qui est
     * retournée.
     *
     * @param command
     * @return
     */
    public static String executeAndBindOutput(@NotNull String[] command) {
        //Excéution de la commande, et récupération
        //des sorties de la commande.
        Map<String, String> outputs = executeBasic(command);
        String stdout = outputs.get("STDOUT");
        String stderr = outputs.get("STDERR");

        String content;
        //Si pas de sortie standard.
        if(stdout.isEmpty()) {
            //On choisit la sortie d'erreur
            //pour le contenu.
            content = stderr;
            //Sinon.
        } else {
            //On choisit la sortie standard
            //pour le contenu.
            content = stdout;
        }

        return content;
    }

    /**
     * Exécuter une commande externe en java
     *
     * @param command
     */
    public static Map<String, String> executeBasic(@NotNull String[] command) {
        Map<String, String> outputs = new HashMap<String, String>();

        try {
            //Exécution de la commande.
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(command);

            //Attente de la fin du processus.
            process.waitFor();

            //Récupération des flux de sortie
            //de la commande.
            String contentOutputStream = readInputStream(process.getInputStream());//Sortie standard.
            String contentErrorStream = readInputStream(process.getErrorStream());//Sortie d'erreur.

            //Sauvegarde des flux de sortie.
            outputs.put("STDOUT", contentOutputStream);
            outputs.put("STDERR", contentErrorStream);
        } catch (Exception exception) {
            HttpServer.getInstance().logErrorAndExit("Erreur lors de l'exécution d'une commande externe : \"" +
                                                              command  + "\" !", exception);
        }

        return outputs;
    }

    /**
     * Obtenir le contenu d'un flux.
     *
     * @param inputStream
     * @return
     */
    private static String readInputStream(@NotNull InputStream inputStream) {
        String content = "";

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            //On accumule chaque ligne au contenu.
            while((line = reader.readLine()) != null) {
                content += line;
            }
        } catch (IOException exception) {
            HttpServer.getInstance().logErrorAndExit("Erreur lors de l'exécution d'une commande externe !", exception);
        }

        return content;
    }
}