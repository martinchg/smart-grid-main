package fr.imta.smartgrid.server;


import fr.imta.smartgrid.model.Consumer;
import fr.imta.smartgrid.model.EVCharger;
import fr.imta.smartgrid.model.Producer;
import fr.imta.smartgrid.model.Sensor;
import fr.imta.smartgrid.model.SolarPanel;
import fr.imta.smartgrid.model.WindTurbine;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class SensorHandler implements Handler<RoutingContext> {
    private final EntityManager db;

    public SensorHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {
        try {
            int id = Integer.parseInt(event.pathParam("id"));
            Sensor sensor = db.find(Sensor.class, id);  // Retrieve grid ID from path

            if (sensor == null) {
                // If no grid is found, return 404
                event.response().setStatusCode(404).end("Grid with ID " + id + " not found.");
                return;
            }

            // Ajoute les mesures disponibles
            JsonArray measurements = new JsonArray();
            sensor.getMeasurements().forEach(measurement -> measurements.add(measurement.getId()));
            
            // Ajoute les propriétaires
            JsonArray owners = new JsonArray();
            sensor.getOwners().forEach(owner -> owners.add(owner.getId()));

            // Create a JSON object to represent the grid
            JsonObject result = new JsonObject();
            result.put("id", sensor.getId());
            result.put("name", sensor.getName());
            result.put("description", sensor.getDescription());
            result.put("kind", getSensorKind(sensor));
            result.put("grid", sensor.getGrid() != null ? sensor.getGrid().getId() : null);
            result.put("available_measurements",measurements);
            result.put("owners", owners);
            addSpecificFields(sensor, result);
            
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
    
        private String getSensorKind(Sensor sensor) {
            if (sensor instanceof SolarPanel) {
                return "SolarPanel";
            } else if (sensor instanceof WindTurbine) {
                return "WindTurbine";
            } else if (sensor instanceof EVCharger) {
                return "EVCharger";
            } else if (sensor instanceof Producer) {
                return "Producer";
            } else if (sensor instanceof Consumer) {
                return "Consumer";
            } else {
                return "Sensor";
            }
        }

        private void addSpecificFields(Sensor sensor, JsonObject response) {
            // Pour les producteurs
            if (sensor instanceof Producer producer) {
                response.put("power_source", producer.getPowerSource());

                if (sensor instanceof SolarPanel solarPanel) {
                    response.put("efficiency", solarPanel.getEfficiency());

                } else if (sensor instanceof WindTurbine windTurbine) {
                    response.put("height", windTurbine.getHeight());
                    response.put("blade_length", windTurbine.getBladeLength());
                }
            }

            // Pour les consommateurs
            if (sensor instanceof Consumer consumer) {
                response.put("max_power", consumer.getMaxPower());

                if (sensor instanceof EVCharger evCharger) {
                    response.put("type", evCharger.getType());
                    response.put("maxAmp", evCharger.getMaxAmp());
                    response.put("voltage", evCharger.getVoltage());
                }
            }
        }
}