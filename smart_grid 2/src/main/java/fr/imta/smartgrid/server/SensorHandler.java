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

// Cette classe gère les requêtes HTTP pour récupérer les informations d'un capteur (Sensor).
public class SensorHandler implements Handler<RoutingContext> {
    private final EntityManager db;

    // Constructeur qui initialise l'EntityManager pour les opérations sur la base de données.
    public SensorHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {
        try {
            // Récupère l'identifiant du capteur depuis les paramètres de la requête.
            int id = Integer.parseInt(event.pathParam("id"));
            Sensor sensor = db.find(Sensor.class, id);  // Recherche du capteur dans la base de données.

            if (sensor == null) {
                // Si aucun capteur n'est trouvé, retourne une erreur 404.
                event.response().setStatusCode(404).end("Sensor with ID " + id + " not found.");
                return;
            }

            // Ajoute les mesures disponibles associées au capteur.
            JsonArray measurements = new JsonArray();
            sensor.getMeasurements().forEach(measurement -> measurements.add(measurement.getId()));
            
            // Ajoute les propriétaires associés au capteur.
            JsonArray owners = new JsonArray();
            sensor.getOwners().forEach(owner -> owners.add(owner.getId()));

            // Crée un objet JSON pour représenter les informations du capteur.
            JsonObject result = new JsonObject();
            result.put("id", sensor.getId()); // Ajoute l'ID du capteur.
            result.put("name", sensor.getName()); // Ajoute le nom du capteur.
            result.put("description", sensor.getDescription()); // Ajoute la description du capteur.
            result.put("kind", getSensorKind(sensor)); // Ajoute le type du capteur.
            result.put("grid", sensor.getGrid() != null ? sensor.getGrid().getId() : null); // Ajoute l'ID du réseau associé.
            result.put("available_measurements", measurements); // Ajoute les mesures disponibles.
            result.put("owners", owners); // Ajoute les propriétaires.
            addSpecificFields(sensor, result); // Ajoute les champs spécifiques au type de capteur.

            // Retourne la réponse HTTP au format JSON.
            event.json(result);

        } catch (NumberFormatException e) {
            // Gère le cas où l'ID fourni n'est pas un entier valide.
            event.response().setStatusCode(400).end("Invalid sensor ID format.");
        } catch (Exception e) {
            // Gère les erreurs inattendues.
            event.response().setStatusCode(500).end("Internal server error: " + e.getMessage());
        }
    }
    
    // Méthode pour déterminer le type de capteur (Sensor).
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

    // Méthode pour ajouter des champs spécifiques en fonction du type de capteur.
    private void addSpecificFields(Sensor sensor, JsonObject response) {
        // Pour les producteurs
        if (sensor instanceof Producer producer) {
            response.put("power_source", producer.getPowerSource()); // Ajoute la source d'énergie.

            if (sensor instanceof SolarPanel solarPanel) {
                response.put("efficiency", solarPanel.getEfficiency()); // Ajoute l'efficacité du panneau solaire.

            } else if (sensor instanceof WindTurbine windTurbine) {
                response.put("height", windTurbine.getHeight()); // Ajoute la hauteur de l'éolienne.
                response.put("blade_length", windTurbine.getBladeLength()); // Ajoute la longueur des pales.
            }
        }

        // Pour les consommateurs
        if (sensor instanceof Consumer consumer) {
            response.put("max_power", consumer.getMaxPower()); // Ajoute la puissance maximale.

            if (sensor instanceof EVCharger evCharger) {
                response.put("type", evCharger.getType()); // Ajoute le type de chargeur.
                response.put("maxAmp", evCharger.getMaxAmp()); // Ajoute l'ampérage maximal.
                response.put("voltage", evCharger.getVoltage()); // Ajoute la tension.
            }
        }
    }
}