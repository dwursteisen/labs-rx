package fr.soat.labs.rx;

import fr.soat.labs.rx.handler.AggregatorHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.handler.StaticFileHandler;
import rx.Observable;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * Created by formation on 28/02/14.
 */
public class Aggregator {

    private static final String LOG_TAG = "Aggregator";

    public static void main(String[] args) throws URISyntaxException {

        File staticDirectory = new File(Aggregator.class.getResource("/static").toURI());

        Future<? extends WebServer> ws = WebServers.createWebServer(9002) // \n
                .add(new StaticFileHandler(staticDirectory))
                .add("/aggregator", new AggregatorHandler())
                .start();

        Observable.from(ws).subscribe((server) -> Logger.getLogger(LOG_TAG).info("Running aggregateur de données at " + server.getUri()));
    }
}
