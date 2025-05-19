package fr.imta.smartgrid.server;

import java.util.List;

import fr.imta.smartgrid.model.DataPoint;
import fr.imta.smartgrid.model.Measurement;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

// Cette classe gère les requêtes HTTP pour récupérer les informations et les valeurs associées à une mesure (Measurement).
public class MeasurementHandler implements Handler<RoutingContext> {
    private final EntityManager db;

    // Constructeur qui initialise l'EntityManager pour les opérations sur la base de données.
    public MeasurementHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {
        try {
            // Récupère le chemin de la requête HTTP.
            String path = event.request().path();
            String ID = event.request().getParam("id");
            int id = Integer.parseInt(ID);

            // Vérifie si la route correspond à "/measurement/:id/values".
            if (path.matches("/measurement/\\d+/values")) {
                // Récupère l'ID de la mesure depuis le chemin.
                String idParam = event.pathParam("id");
                int measurementId = Integer.parseInt(idParam);

                // Recherche la mesure dans la base de données.
                Measurement measurement = db.find(Measurement.class, measurementId);
                if (measurement == null) {
                    // Retourne une erreur 404 si la mesure n'est pas trouvée.
                    event.response().setStatusCode(404).end(new JsonObject().put("error", "Measurement not found").encode());
                    return;
                }

                // Récupère les paramètres optionnels pour la plage de temps (from et to).
                String fromParam = event.request().getParam("from");
                String toParam = event.request().getParam("to");

                long fromTimestamp = fromParam != null ? Long.parseLong(fromParam) : 0;
                long toTimestamp = toParam != null ? Long.parseLong(toParam) : Integer.MAX_VALUE;

                // Requête pour récupérer les DataPoints dans la plage de temps spécifiée.
                TypedQuery<DataPoint> query = db.createQuery(
                        "SELECT dp FROM DataPoint dp WHERE dp.measurement = :measurement " +
                        "AND dp.timestamp >= :fromTime AND dp.timestamp <= :toTime " +
                        "ORDER BY dp.timestamp", DataPoint.class)
                        .setParameter("measurement", measurement)
                        .setParameter("fromTime", fromTimestamp)
                        .setParameter("toTime", toTimestamp);

                List<DataPoint> datapoints = query.getResultList();

                // Construction de la réponse JSON contenant les DataPoints.
                JsonObject response = new JsonObject();
                response.put("sensor_id", measurement.getSensor().getId()); // Ajoute l'ID du capteur associé.
                response.put("measurement_id", measurement.getId()); // Ajoute l'ID de la mesure.

                JsonArray values = new JsonArray();
                for (DataPoint datapoint : datapoints) {
                    JsonObject dataPointJson = new JsonObject();
                    dataPointJson.put("timestamp", datapoint.getTimestamp()); // Ajoute le timestamp du DataPoint.
                    dataPointJson.put("value", datapoint.getValue()); // Ajoute la valeur du DataPoint.
                    values.add(dataPointJson);
                }

                response.put("values", values); // Ajoute la liste des valeurs.

                // Retourne la réponse JSON.
                event.json(response);
                return;
            }

            // Recherche la mesure dans la base de données en fonction de l'ID.
            Measurement measurement = db.find(Measurement.class, id);

            if (measurement == null) {
                // Retourne une erreur 404 si la mesure n'est pas trouvée.
                event.response().setStatusCode(404).end(new JsonObject().put("error", "Measurement not found").encode());
                return;
            }

            // Construction de la réponse JSON contenant les informations de la mesure.
            JsonObject result = new JsonObject();
            result.put("id", measurement.getId()); // Ajoute l'ID de la mesure.

            if (measurement.getSensor() != null) {
                result.put("capteur", measurement.getSensor().getId()); // Ajoute l'ID du capteur associé.
            }

            result.put("name", measurement.getName()); // Ajoute le nom de la mesure.
            result.put("unit", measurement.getUnit()); // Ajoute l'unité de la mesure.

            // Convertit le résultat en JSON et l'envoie comme réponse.
            event.json(result);

        } catch (NumberFormatException e) {
            // Gère les cas où l'ID fourni n'est pas un entier valide.
            event.response().setStatusCode(400).end("Invalid measurement ID format.");
        } catch (Exception e) {
            // Gère les erreurs inattendues.
            event.response().setStatusCode(500).end("Internal server error: " + e.getMessage());
        }
    }
}
