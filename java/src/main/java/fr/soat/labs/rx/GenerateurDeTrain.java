package fr.soat.labs.rx;

import fr.soat.labs.rx.handler.GenerateurDeTrainWebSocketHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import rx.Observable;

import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * Created by formation on 28/02/14.
 */
public class GenerateurDeTrain {

    private static final String LOG_TAG = "Generateur De Train";

    public static void main(String[] args) throws Exception {

        Future<? extends WebServer> ws = WebServers.createWebServer(9000) // \n
                .add("/trains/?", new GenerateurDeTrainWebSocketHandler()) // \n
                .start();

        Observable.from(ws).subscribe(server -> Logger.getLogger(LOG_TAG).info("Running générateur de train at " + server.getUri()));
    }

}
