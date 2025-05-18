package fr.imta.smartgrid.server;

import java.util.List;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class SensorsHandler implements Handler<RoutingContext> {
    private final EntityManager db;

    public SensorsHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {
        try {
            // Prend le sensor kind depuis le path parameter
            String kind = event.pathParam("kind");
            
            // Query for sensor IDs based on kind
            List<Integer> sensorIds;
            
            if ("SolarPanel".equals(kind)) {
                sensorIds = db.createQuery("SELECT s.id FROM SolarPanel s", Integer.class).getResultList();
            } else if ("WindTurbine".equals(kind)) {
                sensorIds = db.createQuery("SELECT w.id FROM WindTurbine w", Integer.class).getResultList();
            } else if ("EVCharger".equals(kind)) {
                sensorIds = db.createQuery("SELECT e.id FROM EVCharger e", Integer.class).getResultList();
            } else {
                // Return le array vide for invalid kind
                sensorIds = List.of();
            }
            
            // Create a JSON array to hold the sensor IDs
            event.end(new JsonArray(sensorIds).encode());
        
        
        } catch (NumberFormatException e) {
            // Handle invalid ID format
            event.response().setStatusCode(400).end("Invalid grid ID format.");
        } catch (Exception e) {
            // Handle unexpected errors
            event.response().setStatusCode(500).end("Internal server error: " + e.getMessage());
        }
    }
}
