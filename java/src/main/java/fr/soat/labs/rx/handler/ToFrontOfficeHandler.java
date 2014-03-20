package fr.soat.labs.rx.handler;

import fr.soat.labs.rx.model.Train;
import org.webbitserver.EventSourceConnection;
import org.webbitserver.EventSourceHandler;
import org.webbitserver.EventSourceMessage;
import rx.Observable;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA. User: david.wursteisen Date: 10/02/14 Time: 15:34 To change this template use File | Settings | File
 * Templates.
 */
public class ToFrontOfficeHandler implements EventSourceHandler {

    private static final String LOG_TAG = "Eventsource";

    private final Observable<Train> observable;

    private final List<EventSourceConnection> connections = new LinkedList<>();

    public ToFrontOfficeHandler(Observable<Train> observable) {
        this.observable = observable;

        this.observable.map(e -> e.serialise())  // \n
                .map(json -> new EventSourceMessage(json)) // \n
                .subscribe(msg -> connections.forEach(c -> c.send(msg)));
    }

    @Override
    public void onOpen(final EventSourceConnection connection) throws Exception {
        Logger.getLogger(LOG_TAG).info("Open SSE connection");
        connections.add(connection);
    }

    @Override
    public void onClose(final EventSourceConnection connection) throws Exception {
        Logger.getLogger(LOG_TAG).info("Close SSE connection");
        connections.remove(connection);

    }

}
