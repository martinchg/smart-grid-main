package fr.imta.smartgrid.server;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class GridActionHandler implements Handler<RoutingContext> {
    private final EntityManager db;

    public GridActionHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {
        String action = event.pathParam("action");
        try {
            int id = Integer.parseInt(event.pathParam("id"));

            switch (action) {
                case "production" -> handleGetMetric(event, id, "total_energy_produced", "producer", "Error calculating production");
                case "consumption" -> handleGetMetric(event, id, "total_energy_consumed", "consumer", "Error calculating consumption");
                default -> event.response().setStatusCode(400).end("Invalid action: " + action);
            }

        } catch (NumberFormatException e) {
            event.response().setStatusCode(400).end("Invalid grid ID format.");
        } catch (Exception e) {
            event.response().setStatusCode(500).end("Internal server error: " + e.getMessage());
        }
    }

    private void handleGetMetric(RoutingContext event, Integer gridId, String metricName, String sensorType, String errorMessage) {
        try {
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

            event.response()
                .putHeader("content-type", "text/plain")
                .end(String.valueOf(totalMetric));
        } catch (Exception e) {
            event.response().setStatusCode(500).end(errorMessage + ": " + e.getMessage());
        }
    }
}