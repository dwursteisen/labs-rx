package fr.soat.labs.rx.handler;

import fr.soat.labs.rx.model.Incident;
import fr.soat.labs.rx.model.Train;
import org.webbitserver.*;
import org.webbitserver.netty.WebSocketClient;
import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;
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


    private Collection<EventSourceConnection> connections = new LinkedList<>();

    public AggregatorHandler() throws URISyntaxException {
        final Subject<Train, Train> trainBroker = PublishSubject.create();
        new WebSocketClient(new URI("ws://localhost:9000/trains"), new MessageHandler<>(trainBroker, (str) -> new Train().deserialise(str))).start();


        final Subject<Incident, Incident> incidentBroker = PublishSubject.create();
        new WebSocketClient(new URI("ws://localhost:9001/incidents"), new MessageHandler<>(incidentBroker, (str) -> new Incident().deserialise(str))).start();


        Observable<String> fluxDeTrainEnJson = trainBroker.map((t) -> t.serialise());
        Observable<String> fluxIncidentsEnJson = incidentBroker.map((i) -> i.serialize());
        Observable.merge(fluxDeTrainEnJson, fluxIncidentsEnJson)
                .doOnNext((json) -> Logger.getLogger("Aggregator").info("json to send : " + json))
                .map((json) -> new EventSourceMessage(json))
                .subscribe((msg) -> {
                    connections.forEach((c) -> c.send(msg));
                });
    }


    @Override
    public void onOpen(EventSourceConnection eventSourceConnection) throws Exception {
        connections.add(eventSourceConnection);
    }

    @Override
    public void onClose(EventSourceConnection eventSourceConnection) throws Exception {
        connections.remove(eventSourceConnection);
    }

    private static class MessageHandler<TYPE> extends BaseWebSocketHandler {

        private final Observer<TYPE> broker;
        private final Func1<String, TYPE> builder;

        private MessageHandler(Observer<TYPE> broker, Func1<String, TYPE> builder) {
            this.broker = broker;
            this.builder = builder;
        }

        @Override
        public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
            this.broker.onNext(builder.call(msg));
        }
    }
}
