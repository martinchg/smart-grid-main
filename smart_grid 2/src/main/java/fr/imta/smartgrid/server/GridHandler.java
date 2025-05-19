package fr.imta.smartgrid.server;

import java.util.LinkedList;

import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Sensor;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

// Cette classe gère les requêtes HTTP pour récupérer les informations d'un réseau électrique (Grid).
public class GridHandler implements Handler<RoutingContext> {
    private final EntityManager db;

    // Constructeur qui initialise l'EntityManager pour les opérations sur la base de données.
    public GridHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {
        try {
            // Récupère l'identifiant du réseau depuis les paramètres de la requête.
            int id = Integer.parseInt(event.pathParam("id"));
            Grid grid = db.find(Grid.class, id);  // Recherche du réseau dans la base de données.

            if (grid == null) {
                // Si aucun réseau n'est trouvé, retourne une erreur 404.
                event.response().setStatusCode(404).end("Réseau avec l'ID " + id + " non trouvé.");
                return;
            }

            // Ajoute les utilisateurs associés au réseau (IDs des utilisateurs).
            LinkedList<Integer> users = new LinkedList<>();
            for (Person person : grid.getPersons()) {
                users.add(person.getId());
            }

            // Ajoute les capteurs associés au réseau (IDs des capteurs).
            LinkedList<Integer> sensors = new LinkedList<>();
            for (Sensor sensor : grid.getSensors()) {
                sensors.add(sensor.getId());
            }

            // Crée un objet JSON pour représenter les informations du réseau.
            JsonObject result = new JsonObject();
            result.put("id", grid.getId()); // Ajoute l'ID du réseau.
            result.put("name", grid.getName()); // Ajoute le nom du réseau.
            result.put("description", grid.getDescription()); // Ajoute la description du réseau.
            result.put("users", users); // Ajoute la liste des utilisateurs.
            result.put("sensors", sensors); // Ajoute la liste des capteurs.

            // Retourne la réponse HTTP au format JSON.
            event.json(result);

        } catch (NumberFormatException e) {
            // Gère le cas où l'ID fourni n'est pas un entier valide.
            event.response().setStatusCode(400).end("Format d'ID de réseau invalide.");
        } catch (Exception e) {
            // Gère les erreurs inattendues.
            event.response().setStatusCode(500).end("Erreur interne du serveur : " + e.getMessage());
        }
    }
}