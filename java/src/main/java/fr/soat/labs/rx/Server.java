package fr.soat.labs.rx;

import fr.soat.labs.rx.handler.FromBackOfficeHandler;
import fr.soat.labs.rx.handler.ToFrontOfficeHandler;
import fr.soat.labs.rx.model.Entity;
import fr.soat.labs.rx.model.Train;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.handler.StaticFileHandler;
import org.webbitserver.netty.WebSocketClient;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import java.io.File;
import java.net.URI;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA. User: david.wursteisen Date: 10/02/14 Time: 15:28 To change this template use File | Settings | File
 * Templates.
 */
public class Server {

    private static final String LOG_TAG = "WebServer";

    public static void main(String[] args) throws Exception {
        Subject<Train, Train> broker = PublishSubject.create();

        File staticDirectory = new File(Server.class.getResource("/static").toURI());

        Future<? extends WebServer> ws = WebServers.createWebServer(9000) // \n
                .add(new StaticFileHandler(staticDirectory)) // \n
                .add("/update", new ToFrontOfficeHandler(broker)) // \n
                .start();

        Logger.getLogger(LOG_TAG).info("Running server at " + ws.get().getUri());


        Future<WebSocketClient> client = new WebSocketClient(new URI(ENV.LOCAL.getUrl()), new FromBackOfficeHandler(broker))
                .start();

        Logger.getLogger(LOG_TAG).info("Client listening at " + client.get().getUri());
    }

    public enum ENV {
        LOCAL("ws://localhost:9001/"),
        REMOTE("ws://????????");
        private final String url;

        private ENV(final String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }

}
