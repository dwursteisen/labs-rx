package fr.soat.labs.rx;

import java.util.logging.Logger;

import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.EventSourceHandler;
import org.webbitserver.HttpHandler;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

/**
 * Created with IntelliJ IDEA.
 * User: david.wursteisen
 * Date: 11/02/14
 * Time: 13:53
 * To change this template use File | Settings | File Templates.
 */
public class PrintToConsole extends BaseWebSocketHandler {

    private static final String LOG_TAG = "websocket";

    private final Subject<WebSocketPacket, WebSocketPacket> reactive = PublishSubject.create();

    public PrintToConsole() {
        reactive.map(new Func1<WebSocketPacket, WebSocketPacket>() {
            @Override
            public WebSocketPacket call(final WebSocketPacket packet) {
                return packet.withMessage(String.format("Received : %s", packet.message));
            }
        }).subscribe(new Action1<WebSocketPacket>() {
            @Override
            public void call(final WebSocketPacket packet) {
                packet.connection.send(packet.message);
            }
        });
    }

    @Override
    public void onMessage(final WebSocketConnection connection, final String msg) throws Throwable {
        Logger.getLogger(LOG_TAG).info("Received "+msg);
        reactive.onNext(new WebSocketPacket(connection, msg));

    }

    @Override
    public void onOpen(final WebSocketConnection connection) throws Exception {
        Logger.getLogger(LOG_TAG).info("Connection Open");
    }

    @Override
    public void onMessage(final WebSocketConnection connection, final byte[] msg) throws Throwable {
        onMessage(connection, new String(msg));
    }

    private static class WebSocketPacket {
        private final WebSocketConnection connection;
        private final String message;

        private WebSocketPacket(final WebSocketConnection connection, final String message) {
            this.connection = connection;
            this.message = message;
        }

        public WebSocketPacket withMessage(final String message) {
            return new WebSocketPacket(connection, message);
        }
    }
}
