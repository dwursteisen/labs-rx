package fr.soat.labs.rx.handler;

import fr.soat.labs.rx.model.Incident;
import fr.soat.labs.rx.model.Train;
import org.webbitserver.*;
import org.webbitserver.netty.WebSocketClient;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Func1;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * Created by formation on 28/02/14.
 */
public class AggregatorHandler implements EventSourceHandler {


    private final Collection<EventSourceConnection> connections = new LinkedList<>();

    public AggregatorHandler() throws URISyntaxException {
        Observable<Train> trainBroker = buildWebsocketObservable("ws://localhost:9000/trains")
                                                                .map(new Train()::deserialise);

        Observable<Incident> incidentBroker = buildWebsocketObservable("ws://localhost:9001/incidents")
                                                                .map(new Incident()::deserialise);


        Observable<String> fluxDeTrainEnJson = trainBroker.map(Train::serialise);
        Observable<String> fluxIncidentsEnJson = incidentBroker.map(Incident::serialize);

        Observable.merge(fluxDeTrainEnJson, fluxIncidentsEnJson)
                .doOnNext((json) -> Logger.getLogger("Aggregator").info("json to send : " + json))
                .map(EventSourceMessage::new)
                .subscribe((msg) -> connections.forEach((c) -> c.send(msg)),
                            (ex) ->  Logger.getLogger("Aggregator").warning(ex.getMessage())
                );
    }

    private Observable<String> buildWebsocketObservable(String url) {
        return Observable.create(new Observable.OnSubscribeFunc<String>() {
            @Override
            public Subscription onSubscribe(Observer<? super String> observer) {
                try {
                    new WebSocketClient(new URI(url), new MessageHandler(observer)).start();
                } catch (URISyntaxException e) {
                    observer.onError(e);
                }
                return Subscriptions.empty();
            }
        }).cache();
    }

    @Override
    public void onOpen(EventSourceConnection eventSourceConnection) throws Exception {
        connections.add(eventSourceConnection);
    }

    @Override
    public void onClose(EventSourceConnection eventSourceConnection) throws Exception {
        connections.remove(eventSourceConnection);
    }

    private static class MessageHandler extends BaseWebSocketHandler {

        private final Observer<? super String> broker;

        private MessageHandler(Observer<? super String> observer) {
            this.broker = observer;
        }

        @Override
        public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
            this.broker.onNext(msg);
        }
    }
}
