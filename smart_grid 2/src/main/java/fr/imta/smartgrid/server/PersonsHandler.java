package fr.imta.smartgrid.server;

import java.util.List;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

// Cette classe gère les requêtes HTTP pour récupérer la liste des identifiants des personnes.
public class PersonsHandler implements Handler<RoutingContext> {
    private EntityManager db;

    // Constructeur qui initialise l'EntityManager pour les opérations sur la base de données.
    public PersonsHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {
        // Exécute une requête SQL native pour récupérer la liste des identifiants des personnes.
        List<Integer> persons = db.createNativeQuery("select id from person").getResultList();

        // Retourne la liste des identifiants des personnes au format JSON dans la réponse HTTP.
        event.json(persons);
    }
}