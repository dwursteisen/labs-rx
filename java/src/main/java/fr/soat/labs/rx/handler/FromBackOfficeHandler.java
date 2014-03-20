package fr.soat.labs.rx.handler;

import fr.soat.labs.rx.model.Connection;
import fr.soat.labs.rx.model.Train;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;
import rx.Observer;

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

    private final Observer<Train> listeners;


    public FromBackOfficeHandler(Observer<Train> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void onOpen(final WebSocketConnection connection) throws Exception {
        connection.send(Connection.CONNECTED.toString());
    }

    @Override
    public void onMessage(final WebSocketConnection connection, final String msg) throws Throwable {
        Logger.getLogger(LOG_TAG).info("Receive Message : " + msg);
        listeners.onNext(new Train().deserialise(msg));
    }
}
