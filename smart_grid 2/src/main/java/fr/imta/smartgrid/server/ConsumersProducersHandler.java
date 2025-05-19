// PARTIE: Des capteurs au backend (GET /consumers and GET /producers)

package fr.imta.smartgrid.server;

import java.util.List;

import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Sensor;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class ConsumersProducersHandler implements Handler<RoutingContext> {
    EntityManager db;

    public ConsumersProducersHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {
        String chemin = event.request().path();
        String[] segments = chemin.split("/"); // ["", "consumers"] ou ["", "producers"]
        String choix = segments[1];

        if (choix.equals("consumers")) {
            @SuppressWarnings("unchecked")
            List<Integer> consumers = this.db.createNativeQuery("SELECT id FROM consumer").getResultList();
            JsonArray resultat = new JsonArray();
            for (int id:consumers) {
                Sensor sensor = this.db.find(Sensor.class, id);
                this.db.refresh((sensor));
                String type = (String) this.db.createNativeQuery("SELECT dtype FROM sensor WHERE id=?")
                                                .setParameter(1, id)
                                                .getSingleResult();
                @SuppressWarnings("unchecked")
                List<Integer> measurements_list = this.db.createNativeQuery("select id from measurement where sensor=?")
                                                            .setParameter(1, id)
                                                            .getResultList();
                JsonArray measurements = new JsonArray(measurements_list);
                @SuppressWarnings("unchecked")
                List<Integer> owners_list = this.db.createNativeQuery("select person_id from person_sensor where sensor_id=?")
                                                    .setParameter(1, id)
                                                    .getResultList();
                JsonArray owners = new JsonArray(owners_list);

                Grid grid = sensor.getGrid();
                Integer grid_id = null;
                if (grid != null) {
                    grid_id = grid.getId();
                }

                JsonObject capteur  = new JsonObject()
                    .put("id", id)
                    .put("name", sensor.getName())
                    .put("description", sensor.getDescription())
                    .put("kind", type)
                    .put("grid", grid_id)
                    .put("available_measurements", measurements)
                    .put("owners", owners)
                    .put("max_power", this.db.createNativeQuery("SELECT max_power FROM consumer WHERE id=?")
                                                    .setParameter(1, id)
                                                    .getSingleResult())
                    .put("type", this.db.createNativeQuery("SELECT connector_type FROM ev_charger WHERE id=?")
                                                    .setParameter(1, id)
                                                    .getSingleResult())
                    .put("maxAmp", this.db.createNativeQuery("SELECT maxamp FROM ev_charger WHERE id=?")
                                                    .setParameter(1, id)
                                                    .getSingleResult())
                    .put("voltage", this.db.createNativeQuery("SELECT voltage FROM ev_charger WHERE id=?")
                                                    .setParameter(1, id)
                                                    .getSingleResult());                
                resultat.add(capteur);
            }
            event.response()
            .setStatusCode(200)
            .putHeader("Content-Type", "application/json")
            .end(resultat.encodePrettily());
        }
        else if (choix.equals("producers")) {
            @SuppressWarnings("unchecked")
            List<Integer> producers = this.db.createNativeQuery("SELECT id FROM producer").getResultList();
            JsonArray resultat = new JsonArray();
            for (int id:producers) {
                Sensor sensor = this.db.find(Sensor.class, id);
                this.db.refresh((sensor));
                String type = (String) this.db.createNativeQuery("SELECT dtype FROM sensor WHERE id=?")
                                                .setParameter(1, id)
                                                .getSingleResult();
                @SuppressWarnings("unchecked")
                List<Integer> measurements_list = this.db.createNativeQuery("select id from measurement where sensor=?")
                                                            .setParameter(1, id)
                                                            .getResultList();
                JsonArray measurements = new JsonArray(measurements_list);

                @SuppressWarnings("unchecked")
                List<Integer> owners_list = this.db.createNativeQuery("select person_id from person_sensor where sensor_id=?")
                                                    .setParameter(1, id)
                                                    .getResultList();
                JsonArray owners = new JsonArray(owners_list);

                Grid grid = sensor.getGrid();
                Integer grid_id = null;
                if (grid != null) {
                    grid_id = grid.getId();
                }

                JsonObject capteur  = new JsonObject()
                    .put("id", id)
                    .put("name", sensor.getName())
                    .put("description", sensor.getDescription())
                    .put("kind", type)
                    .put("grid", grid_id)
                    .put("available_measurements", measurements)
                    .put("owners", owners);
                if (type.equals("SolarPanel")) {
                    capteur
                    .put("power_source", this.db.createNativeQuery("SELECT power_source FROM producer WHERE id=?")
                                                    .setParameter(1, id)
                                                    .getSingleResult())
                    .put("efficiency", this.db.createNativeQuery("SELECT efficiency FROM solar_panel WHERE id=?")
                                                    .setParameter(1, id)
                                                    .getSingleResult());
                }
                else if (type.equals("WindTurbine")) {
                    capteur
                    .put("power_source", this.db.createNativeQuery("SELECT power_source FROM producer WHERE id=?")
                                                    .setParameter(1, id)
                                                    .getSingleResult())
                    .put("height", this.db.createNativeQuery("SELECT height FROM wind_turbine WHERE id=?")
                                                    .setParameter(1, id)
                                                    .getSingleResult())
                    .put("blade_length", this.db.createNativeQuery("SELECT bladelength FROM wind_turbine WHERE id=?")
                                                    .setParameter(1, id)
                                                    .getSingleResult());
                }
                resultat.add(capteur);
            }
            event.response()
            .setStatusCode(200)
            .putHeader("Content-Type", "application/json")
            .end(resultat.encodePrettily());
        }
    }
}