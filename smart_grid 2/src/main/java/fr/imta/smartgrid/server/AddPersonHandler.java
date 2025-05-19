package fr.imta.smartgrid.server;

import java.util.LinkedList;

import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Sensor;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

// Cette classe gère les requêtes HTTP pour récupérer les informations d'une personne.
public class AddPersonHandler implements Handler<RoutingContext> {
    private EntityManager db;

    // Constructeur qui initialise l'EntityManager pour les opérations sur la base de données.
    public AddPersonHandler (EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {
        // Récupère l'identifiant de la personne depuis les paramètres de la requête.
        int id = Integer.parseInt(event.pathParam("id"));

        // Recherche la personne dans la base de données en utilisant son ID.
        Person p = (Person) db.find(Person.class, id);

        // Crée un objet JSON pour représenter les informations de la personne.
        JsonObject result = new JsonObject();
        result.put("id", id); // Ajoute l'ID de la personne.
        result.put("first_name", p.getFirstName()); // Ajoute le prénom de la personne.
        result.put("last_name", p.getLastName()); // Ajoute le nom de famille de la personne.
        result.put("grid", p.getGrid().getId()); // Ajoute l'ID du réseau auquel la personne est associée.

        // Ajoute la liste des capteurs possédés par la personne (IDs des capteurs).
        LinkedList<Integer> l = new LinkedList<>();
        for (Sensor s : p.getSensors()) {
            l.add(s.getId());
        }
        result.put("owned_sensors", l); // Ajoute la liste des capteurs possédés.

        // Retourne les informations de la personne au format JSON dans la réponse HTTP.
        event.json(result);
    }
}