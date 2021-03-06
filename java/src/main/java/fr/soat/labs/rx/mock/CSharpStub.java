package fr.soat.labs.rx.mock;

import fr.soat.labs.rx.model.Train;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.WebSocketConnection;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.util.functions.Action0;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 24/02/14
 * Time: 10:01
 * To change this template use File | Settings | File Templates.
 */
public class CSharpStub {
    private final static String LOG_TAG = "CSharpStub";

    public static void main(String[] args) throws Exception {
        Future<? extends WebServer> ws = WebServers.createWebServer(9001) // \n
                .add("/?", new CSharpStubHandler())  // \n
                .start();

        Logger.getLogger(LOG_TAG).info("Running CSharp Stub server at " + ws.get().getUri());

    }

    private static class CSharpStubHandler extends BaseWebSocketHandler {

        private final Collection<WebSocketConnection> connections = new LinkedList<>();

        private CSharpStubHandler() {
            Action0 sendMessageToEveryOne = () -> {
                Observable.from(connections).subscribe(ws -> {
                    Train train = new Train();
                    train.id = generateId();
                    ws.send(train.serialise());
                });
            };
            Schedulers.newThread().schedulePeriodically(sendMessageToEveryOne, 0, 1, TimeUnit.SECONDS);
        }

        private static int nbGeneratedTrain = 0;

        private String generateId() {
            synchronized (this) {
                nbGeneratedTrain++;
                return "" + nbGeneratedTrain;
            }
        }

        @Override
        public void onOpen(WebSocketConnection connection) throws Exception {
            connections.add(connection);
            Logger.getLogger(LOG_TAG).info("Connection added : " + connection);

        }

        @Override
        public void onClose(WebSocketConnection connection) throws Exception {
            connections.remove(connection);
            Logger.getLogger(LOG_TAG).info("Connection removed : " + connection);
        }
    }
}
