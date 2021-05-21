# Projet serveur HTTP GET

____

### Résumé

Projet implémentant un serveur HTTP ne gérant que les routes GET
pour HTTP 1.0 et HTTP 1.1

____

### Organisation

Université de Lorraine
<br>
Master 1 MIAGE

____

### Auteurs

- CHEVRIER Jean-Christophe
- HADJ MESSAOUD Yousra
- LOUGADI Marième

________

### Exécution

    Sous linux:     ./server_run.sh
    Sous windows:   .\server_run.bat

____

### Langages

Nous avons développé et compilé en <b>JAVA 1.8</b> le projet

____

### Librairies

Nous avons utilisé la librairie des annotations @NotNull de Jetbrains (qui servent juste à lever des exceptions à la 
réception de valeur null) : https://mvnrepository.com/artifact/org.jetbrains/annotations/20.1.0

______

### Notes

- Toutes les fonctionnalités facultatives ont été implémentées hormis l'envoi des assets (.js et .css) en fichiers 
  compressés au navigateur
  
______

### Configuration
    
    configuration/
        configuration.properties    paramètres généraux du serveur
        content_typs.properies      référencement extension de fichier - type de contenu HTTP du serveur
        hosts.properties            référencement hôte - répertoire de document du serveur

_____

#### Gestion des paramètres généraux

Ces paramètres servent à paramétrer la configuration réseau du serveur :
- `address=127.0.0.1`
- `port=80`
- `countMaxConnections=50`

Le paramètre suivant permet de renseigner le répertoire racine des documents du serveur :
`rootPathDocuments=document`

Si vous voulez le modifier, notez qu'il doit toujours être au même niveau que le /bin, et les exécutables

Les paramètres suivants servent à gérer le système de pages web affichant le système d'arborescence du serveur :
- `treeDocumentsURI=/tree`          pour définir l'URI permettant d'accéder à l'arborescence
- `treeDocumentsEnable=true`        pour définir si le système d'arborescence est activé et donc accessible 

Actuellement, pour se rendre aux pages web du système affichant l'arborescence du serveur, vous devez vous 
rendre à cet URL : `http://adresseDuServeur:portEcouteDuServeur/tree`

Si le système d'arborescence n'est pas activé, une erreur 404 est envoyé au client web

_____

#### Gestion des types de contenu

Quand vous ajoutez un fichier à vos documents du serveur dont l'extension n'est pas renseignée dans ce fichier, vous devez 
la référencer, en ajoutant une ligne, selon ce format : `.extension=typeDeContenu`

_____

#### Gestion des virtual hosts

Quand vous mettez à jour un virtual host sur la machine du serveur, vous devez mettre à jour le fichier hosts.properties
comme il se doit : `virtualHost=répertoireDeDocuments`

Exemple : `www.dopetrope.com` a comme index : `wwww.dopetrope.com/index.html` et correspond au répertoire : `dropetope/`

Note : pour que les virtual hosts renseignés au départ dans le fichier `hosts.properties` fonctionnent, vous devez mettre 
à jour votre fichier de configuration réseau de hosts de votre OS, et ce en ajoutant une ligne telle que celle-ci pour 
chaque virtual host :`virtualHost  adresseDuServeur`

Sous windows, ce fichier de configuration réseau de hosts se trouve à ce chemin :
`c:\windows\system32\drivers\etc\hosts`

Voici la liste des routes d'accès aux indexs des documents du serveur
en utilisant les virtual hosts :
- `www.dopetrope.com/index.html`
- `www.verti.com/index.html`
- `www.test_1.com/html/index.html`
- `www.test_2.com/html/index.html`
- `www.test_3.com/html/index.html`
- `www.test_4.com/html/index.html`
- `www.test_5.com/html/index.html`
- `www.test_6.com/html/index.html`
- `www.test_7.com/php/index.php`
- `www.test_7.com/php/index2.php`

______

### Architecture logicielle
    
    src/
        core/   fonctionnalités essentielles du serveur
        tool/   fonctionnalités supplémentaires non essentielles du serveur ou programmes utilitaires

_____

### Distribution binaire

    bin/    sources compilées utilisées par les éxecutables .bat et .sh du serveur

____

### Documents

    document/       répertoire des documents / sites hébergés par le serveur
        test.*/     documents du serveur qui ont servis de tests itératifs au cours du développement du serveur
        ...         vrais sites récupérés de arche 

____

### Contenus dynamiques

Ce serveur accepte et gère les documents de type .php, envoyant des contenus dynamiques 