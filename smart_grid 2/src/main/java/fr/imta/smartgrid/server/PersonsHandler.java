package fr.imta.smartgrid.server;


import java.util.List;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class PersonsHandler implements Handler<RoutingContext> {
    private EntityManager db;
    public PersonsHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {
        List<Integer> persons = db.createNativeQuery("select id from person").getResultList();
        event.json(persons);

    }
    
}