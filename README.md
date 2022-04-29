# À propos du widget Bookmark

* Licence : [AGPL v3](http://www.gnu.org/licenses/agpl.txt) - Copyright Région Hauts-de-France (ex Picardie), Département Essonne, Région Nouvelle Aquitaine (ex Poitou-Charente)
* Développeur(s) : ATOS, Open Digital Education
* Financeur(s) : Région Hauts-de-France (ex Picardie), Département Essonne, Région Nouvelle Aquitaine (ex Poitou-Charente)
* Description : Widget de gestion de signets personnels

# Documentation technique

## Construction

<pre>
		gradle copyMod
</pre>

## Déployer dans ent-core

## Configuration

Dans le fichier `/bookmark/deployment/bookmark/conf.json.template` :

Déclarer l'application dans la liste :
<pre>
{
  "name": "net.atos~bookmark~0.3.0",
      "config": {
	    "main" : "net.atos.entng.bookmark.Bookmark",
	    "port" : 8032,
	    "app-name" : "Signets",
	    "app-address" : "/bookmark",
	    "app-icon" : "bookmark-large",
	    "host": "${host}",
	    "ssl" : $ssl,
	    "auto-redeploy": false,
	    "userbook-host": "${host}",
	    "integration-mode" : "HTTP",
	    "app-registry.port" : 8012,
	    "mode" : "${mode}",
	    "entcore.port" : 8009
      }
}
</pre>

Associer une route d'entée à la configuration du module proxy intégré (`"name": "net.atos~bookmark~0.3.0"`) :
<pre>
	{
		"location": "/bookmark",
		"proxy_pass": "http://localhost:8032"
	}
</pre>

# Présentation du module

## Fonctionnalités

Le Widget Bookmark permet une gestion de signets personnels.

## Modèle de persistance

Les données du module sont stockées dans une collection Mongo :
 - "bookmark" : pour toutes les données propres aux signets

## Modèle serveur

Le module serveur utilise un contrôleur de déclaration :

* `BookmarkController` : Point d'entrée du widget, Routage des vues, sécurité globale et déclaration de l'ensemble des comportements relatifs aux signets (liste, création, modification et destruction)

Le contrôleur étend les classes du framework Ent-core exploitant les CrudServices de base. Pour des manipulations spécifiques, des classes de Service sont utilisées :

* `BookmarkService` : Concernant les comportements de la gestion des signets

Un jsonschema permet de vérifier les données reçues par le serveur, ils se trouvent dans le dossier "src/main/resources/jsonschema".

## Modèle front-end

Le modèle Front-end manipule un objet model :

* `bookmarks` : Correspondant aux signets

Il y a une collection globale :

* `model.bookmarks.all` qui contient l'ensemble des objets `bookmark` synchronisé depuis le serveur.
