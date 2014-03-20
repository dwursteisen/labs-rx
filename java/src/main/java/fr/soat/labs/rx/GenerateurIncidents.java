package fr.soat.labs.rx;

import fr.soat.labs.rx.handler.GenerateurIncidentsHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.handler.StaticFileHandler;

import java.io.File;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * Created by formation on 28/02/14.
 */
public class GenerateurIncidents {

    private static final String LOG_TAG = "GenerateurIncidents";


    public static void main(String[] args) throws Exception {

        File staticDirectory = new File(Server.class.getResource("/static").toURI());


        Future<? extends WebServer> ws = WebServers.createWebServer(9001) // \n
                .add(new StaticFileHandler(staticDirectory))
                .add("/incidents/?", new GenerateurIncidentsHandler()) // \n
                .start();

        Logger.getLogger(LOG_TAG).info("Running générateur de train at " + ws.get().getUri());



    }
}
