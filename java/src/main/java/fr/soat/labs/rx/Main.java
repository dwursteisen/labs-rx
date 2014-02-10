package fr.soat.labs.rx;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.handler.StaticFileHandler;

/**
 * Created with IntelliJ IDEA. User: david.wursteisen Date: 10/02/14 Time: 15:28 To change this template use File | Settings | File
 * Templates.
 */
public class Main {

    private static final String LOG_TAG = "WebServer";

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Future<? extends WebServer> ws = WebServers.createWebServer(9000) // \n
                .add(new StaticFileHandler("./static")) // \n
                .add("/hello", new HelloWorld()) // \n
                .start();

        Logger.getLogger(LOG_TAG).info("Running server at "+ws.get().getUri());
    }

}
