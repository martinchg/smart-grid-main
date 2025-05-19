package fr.imta.smartgrid.server;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

// Cette classe gère les requêtes HTTP pour les actions liées au réseau électrique (production, consommation).
public class GridActionHandler implements Handler<RoutingContext> {
    private final EntityManager db;

    // Constructeur qui initialise l'EntityManager pour les opérations sur la base de données.
    public GridActionHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {
        // Récupère l'action (production ou consommation) depuis les paramètres de la requête.
        String action = event.pathParam("action");
        try {
            // Récupère l'identifiant du réseau depuis les paramètres de la requête.
            int id = Integer.parseInt(event.pathParam("id"));

            // Exécute l'action demandée en fonction de la valeur de "action".
            switch (action) {
                case "production" -> handleGetMetric(event, id, "total_energy_produced", "producer", "Erreur lors du calcul de la production");
                case "consumption" -> handleGetMetric(event, id, "total_energy_consumed", "consumer", "Erreur lors du calcul de la consommation");
                default -> event.response().setStatusCode(400).end("Action invalide : " + action);
            }

        } catch (NumberFormatException e) {
            // Retourne une erreur 400 si l'identifiant du réseau n'est pas un entier valide.
            event.response().setStatusCode(400).end("Format d'identifiant de réseau invalide.");
        } catch (Exception e) {
            // Retourne une erreur 500 en cas d'erreur interne.
            event.response().setStatusCode(500).end("Erreur interne du serveur : " + e.getMessage());
        }
    }

    // Méthode auxiliaire pour calculer une métrique spécifique (production ou consommation) à partir de la base de données.
    private void handleGetMetric(RoutingContext event, Integer gridId, String metricName, String sensorType, String errorMessage) {
        try {
            // Requête SQL pour calculer la somme des valeurs des derniers points de données pour un réseau donné.
            Double totalMetric = (Double) db.createNativeQuery(
                "SELECT COALESCE(SUM(d.value), 0) " +
                "FROM datapoint d " +
                "JOIN measurement m ON d.measurement = m.id " +
                "JOIN sensor s ON m.sensor = s.id " +
                "JOIN " + sensorType + " t ON t.id = s.id " +
                "WHERE s.grid = ?1 " +
                "AND m.name = ?2 " +
                "AND d.timestamp = (SELECT MAX(d2.timestamp) FROM datapoint d2 WHERE d2.measurement = d.measurement)")
                .setParameter(1, gridId)
                .setParameter(2, metricName)
                .getSingleResult();

            // Retourne la métrique calculée dans la réponse HTTP.
            event.response()
                .putHeader("content-type", "text/plain")
                .end(String.valueOf(totalMetric));
                
        } catch (NumberFormatException e) {
            // Retourne une erreur 400 si l'identifiant du réseau n'est pas un entier valide.
            event.response().setStatusCode(400).end("Format d'identifiant de réseau invalide.");
        } catch (Exception e) {
            // Retourne une erreur 500 si la requête SQL échoue.
            event.response().setStatusCode(500).end(errorMessage + ": " + e.getMessage());
        }
    }
}