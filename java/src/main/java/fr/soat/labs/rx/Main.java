package fr.soat.labs.rx;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.handler.StaticFileHandler;
import org.webbitserver.netty.WebSocketClient;

/**
 * Created with IntelliJ IDEA. User: david.wursteisen Date: 10/02/14 Time: 15:28 To change this template use File | Settings | File
 * Templates.
 */
public class Main {

    private static final String LOG_TAG = "WebServer";

    public static void main(String[] args) throws Exception {
        Future<? extends WebServer> ws = WebServers.createWebServer(9000) // \n
                .add(new StaticFileHandler("./static")) // \n
                .add("/hello", new HelloWorld()) // \n
                .add("/?ws", new PrintToConsole())
                .start();

        Logger.getLogger(LOG_TAG).info("Running server at "+ws.get().getUri());

        new WebSocketClient(new URI("ws://localhost:81/"), new BaseWebSocketHandler() {
            @Override
            public void onOpen(final WebSocketConnection connection) throws Exception {
                connection.send("dwursteisen");
            }

            @Override
            public void onMessage(final WebSocketConnection connection, final String msg) throws Throwable {
                Logger.getLogger(LOG_TAG).info("onMessage : " + msg);
//                connection.send(msg.toUpperCase());
            }
        }).start();
    }

}
