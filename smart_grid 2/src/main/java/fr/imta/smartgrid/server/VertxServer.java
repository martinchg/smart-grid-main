package fr.imta.smartgrid.server;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.persistence.config.PersistenceUnitProperties.CONNECTION_POOL_MIN;
import static org.eclipse.persistence.config.PersistenceUnitProperties.LOGGING_LEVEL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.TARGET_SERVER;
import org.eclipse.persistence.config.TargetServer;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;

public class VertxServer {
    private Vertx vertx;
    private EntityManager db; // database object

    public VertxServer() {
        this.vertx = Vertx.vertx();

        // setup database connexion
        Map<String, String> properties = new HashMap<>();

        properties.put(LOGGING_LEVEL, "FINE");
        properties.put(CONNECTION_POOL_MIN, "1");

        properties.put(TARGET_SERVER, TargetServer.None);

        var emf = Persistence.createEntityManagerFactory("smart-grid", properties);
        db = emf.createEntityManager();
    }

    public void start() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.get("/hello").handler(new DisplayRequestHandler(this.db));

        router.get("/grids").handler(new GridsHandler(this.db));
        router.get("/grid/:id").handler(new GridHandler(this.db));
        router.get("/grid/:id/:action").handler(new GridActionHandler(this.db));

        router.get("/persons").handler(new PersonsHandler(this.db));
        router.get("/person/:id").handler(new PersonHandler(this.db));
        // router.post("/person/:id").handler(new PersonHandler(this.db));
        // router.delete("/person/:id").handler(new PersonHandler(this.db));
        // router.put("/person").handler(new PersonHandler(this.db));

        router.get("/sensor/:id").handler(new SensorHandler(this.db));
        // router.post("/sensor/:id").handler(new SensorHandler(this.db));
        router.get("/sensors/:kind").handler(new SensorsHandler(this.db));

        // router.get("/consumers").handler(new ConsumersHandler());
        // router.get("/producers").handler(new ProducersHandler());

        router.get("/measurement/:id").handler(new MeasurementHandler(this.db));
        router.get("/measurement/:id/values").handler(new MeasurementHandler(this.db));

        // router.post("/ingress/windturbine").handler(new IngressHandler());

        // start the server
        vertx.createHttpServer().requestHandler(router).listen(8080);
    }

    public static void main(String[] args) {
        new VertxServer().start();
    }


}
