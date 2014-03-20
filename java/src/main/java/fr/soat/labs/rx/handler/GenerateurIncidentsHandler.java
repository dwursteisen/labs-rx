package fr.soat.labs.rx.handler;

import fr.soat.labs.rx.model.Incident;
import fr.soat.labs.rx.model.Train;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.netty.WebSocketClient;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;
import rx.subscriptions.Subscriptions;

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

    private static final String LOG_TAG = "Incidents";
    private final Collection<WebSocketConnection> connections = new LinkedList<>();


    public GenerateurIncidentsHandler() throws URISyntaxException {
        buildTrainObservable()
                .map((t) -> new Incident(t.id, "Problème voyageur", t))
                .delay(5, TimeUnit.SECONDS)
                .filter((i) -> i.id.contains("1") || i.id.contains("2"))
                .map(Incident::serialize)
                .doOnNext((json) -> Logger.getLogger(LOG_TAG).info("incident json to send : " + json))
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

    private Observable<Train> buildTrainObservable() {
        return Observable.create(observer -> {
            try {
                WebSocketHandler wsHandler = new BaseWebSocketHandler() {
                    @Override
                    public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
                        Train train = new Train().deserialise(msg);
                        Logger.getLogger(LOG_TAG).info("new train received on websocket: " + train);
                        observer.onNext(train);
                    }
                };
                Future<WebSocketClient> wsClient = new WebSocketClient(
                        new URI("ws://localhost:9000/trains"),
                        wsHandler)
                        .start();

                Observable.from(wsClient).subscribe((ws) -> Logger.getLogger(LOG_TAG).info("En ecoute sur " + ws.getUri()));
            } catch (URISyntaxException e) {
                observer.onError(e);
            }
            return Subscriptions.empty();
        });

    }

}
