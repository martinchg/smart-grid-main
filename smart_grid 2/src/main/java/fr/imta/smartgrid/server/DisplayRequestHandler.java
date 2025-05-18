package fr.imta.smartgrid.server;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class DisplayRequestHandler implements Handler<RoutingContext> {
    private EntityManager db;
    public DisplayRequestHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {
        System.out.println("Route called: " + event.currentRoute().getName());
        System.out.println("Request HTTP method: " + event.request().method());
        System.out.println("We received these query parameters: " + event.queryParams());
        System.out.println("We have these path params: " + event.pathParams());
        System.out.println("Value for the path param 'name': " + event.pathParam("name"));
        System.out.println("Value for the path param 'lastname': " + event.pathParam("lastname"));
        System.out.println("We received this body: " + event.body().asString());


        // send the response with content "Hello World!"
        String name = event.pathParam("name");
        String lastname = event.pathParam("lastname");
        JsonObject result = new JsonObject();

        if (lastname != null) {
            //event.end("Hello " + name + " " + lastname + "!");
            result.put("msg", "Hello " + name + " " + lastname + "!");
            result.put("name ", name);
            result.put("lastname ", lastname);
            event.json(result);
        } else
        if (name != null) {
            //event.end("Hello " + name + "!");
            result.put("msg", "Hello " + name + "!");
            result.put("name ", name);
            event.json(result);
        } else {
            //event.end("Hello World!");
            result.put("msg", "Hello world!");
            result.put("name ", "anonymous");
            event.json(result);
        }

    }
    
}
