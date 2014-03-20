package fr.soat.labs.rx.handler;

import fr.soat.labs.rx.model.Connection;
import fr.soat.labs.rx.model.Incident;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;
import rx.Observer;

import java.util.logging.Logger;

/**
 * Created by formation on 28/02/14.
 */
public class IncidentHandler extends BaseWebSocketHandler {

    private static final String LOG_TAG = "websocket";

    private final Observer<Incident> listeners;

    public IncidentHandler(Observer<Incident> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void onOpen(final WebSocketConnection connection) throws Exception {
        connection.send(Connection.CONNECTED.toString());
    }

    @Override
    public void onMessage(final WebSocketConnection connection, final String msg) throws Throwable {
        Logger.getLogger(LOG_TAG).info("Receive Message : " + msg);
        Incident incident = new Incident().deserialise(msg);
        listeners.onNext(incident);
    }
}
