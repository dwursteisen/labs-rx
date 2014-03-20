package fr.soat.labs.rx.handler;

import fr.soat.labs.rx.model.Incident;
import fr.soat.labs.rx.model.Train;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.netty.WebSocketClient;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by formation on 28/02/14.
 */

public class GenerateurIncidentsHandler extends BaseWebSocketHandler {

    private static final String LOG_TAG = "Eventsource";
    private final Collection<WebSocketConnection> connections = new LinkedList<>();

    private Subject<Train, Train> incidents = PublishSubject.create();


    public GenerateurIncidentsHandler() throws URISyntaxException {
        Future<WebSocketClient> client =
                new WebSocketClient(new URI("ws://localhost:9000/trains"), new TrainClientWebSockekHandler())
                        .start();
        //
        incidents.map((t) -> {
            Incident incident = new Incident();
            incident.id = t.id;
            incident.message = "Problème voyageur";
            incident.train = t;
            return incident;
        }).delay(5, TimeUnit.SECONDS)
                .filter((i) -> i.id.contains("1") || i.id.contains("2"))
                .map(i -> i.serialize())
                .doOnNext((json) -> Logger.getLogger("Aggregator").info("incident json to send : "+json))
                .subscribe((json) -> connections.forEach(c -> c.send(json)));
    }


    @Override
    public void onOpen(WebSocketConnection eventSourceConnection) throws Exception {
        connections.add(eventSourceConnection);
    }

    @Override
    public void onClose(WebSocketConnection eventSourceConnection) throws Exception {
        connections.remove(eventSourceConnection);
    }

    private class TrainClientWebSockekHandler extends BaseWebSocketHandler {
        @Override
        public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
            Train train = new Train().deserialise(msg);
            Logger.getLogger(LOG_TAG).info("new train " + train);
            incidents.onNext(train);
        }
    }
}
