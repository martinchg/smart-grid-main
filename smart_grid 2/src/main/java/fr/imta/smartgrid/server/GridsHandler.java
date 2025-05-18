package fr.imta.smartgrid.server;


import java.util.List;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class GridsHandler implements Handler<RoutingContext> {
    private EntityManager db;
    
    public GridsHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {
        List<Integer> grids = db.createNativeQuery("select id from grid").getResultList();
        event.json(grids);

    }
    
}