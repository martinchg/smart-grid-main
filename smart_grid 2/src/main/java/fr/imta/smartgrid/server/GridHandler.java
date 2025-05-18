package fr.imta.smartgrid.server;

import java.util.LinkedList;
import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Sensor;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class GridHandler implements Handler<RoutingContext> {
    private final EntityManager db;

    public GridHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {
        try {
            int id = Integer.parseInt(event.pathParam("id"));
            Grid grid = db.find(Grid.class, id);  // Retrieve grid ID from path

            if (grid == null) {
                // If no grid is found, return 404
                event.response().setStatusCode(404).end("Grid with ID " + id + " not found.");
                return;
            }

            // Add users (IDs of users associated with the grid)
            LinkedList<Integer> users = new LinkedList<>();
            for (Person person : grid.getPersons()) {
                users.add(person.getId());
            }

            // Add sensors (IDs of sensors associated with the grid)
            LinkedList<Integer> sensors = new LinkedList<>();
            for (Sensor sensor : grid.getSensors()) {
                sensors.add(sensor.getId());
            }

            // Create a JSON object to represent the grid
            JsonObject result = new JsonObject();
            result.put("id", grid.getId());
            result.put("name", grid.getName());
            result.put("description", grid.getDescription());
            result.put("users", users);
            result.put("sensors", sensors);

            // Return the JSON response
            event.json(result);

        } catch (NumberFormatException e) {
            // Handle invalid ID format
            event.response().setStatusCode(400).end("Invalid grid ID format.");
        } catch (Exception e) {
            // Handle unexpected errors
            event.response().setStatusCode(500).end("Internal server error: " + e.getMessage());
        }
    }

}