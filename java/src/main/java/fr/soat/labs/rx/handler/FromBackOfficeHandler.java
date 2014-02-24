package fr.soat.labs.rx.handler;

import fr.soat.labs.rx.model.Connection;
import fr.soat.labs.rx.model.Entity;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;
import rx.Observer;
import rx.subjects.Subject;

import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: david.wursteisen
 * Date: 11/02/14
 * Time: 13:53
 * To change this template use File | Settings | File Templates.
 */
public class FromBackOfficeHandler extends BaseWebSocketHandler {

    private static final String LOG_TAG = "websocket";

    private final Observer<Entity> listeners;


    public FromBackOfficeHandler(Observer<Entity> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void onOpen(final WebSocketConnection connection) throws Exception {
        connection.send(Connection.CONNECTED.toString());
    }

    @Override
    public void onMessage(final WebSocketConnection connection, final String msg) throws Throwable {
        Logger.getLogger(LOG_TAG).info("Receive Message : " + msg);
        listeners.onNext(new Entity().deserialise(msg));
    }
}
