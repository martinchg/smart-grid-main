package fr.imta.smartgrid.server;


import java.util.LinkedList;

import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Sensor;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class PersonHandler implements Handler<RoutingContext> {
    private EntityManager db;
    public PersonHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {
        int id = Integer.parseInt(event.pathParam("id"));
        Person p = (Person) db.find(Person.class, id);
        JsonObject result = new JsonObject();
        result.put("id", id);
        result.put("first_name", p.getFirstName());
        result.put("last_name", p.getLastName());
        result.put("grid", p.getGrid().getId());
        
        LinkedList<Integer> l = new LinkedList<>();
        for (Sensor s : p.getSensors()){
            l.add(s.getId());
        };
        result.put("owned_sensors", l);
        event.json(result);
        // or if you added a toJson method in Person class
        //event.json(p.toJson());

    }
    
}