package fr.soat.labs.rx.handler;

import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;
import rx.Observer;

public class WebSocketClientOperation extends BaseWebSocketHandler {
    private final Observer<String> datas;

    public WebSocketClientOperation(Observer<String> data) {
        datas = data;
    }

    @Override
    public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
        datas.onNext(msg);
    }

    @Override
    public void onClose(WebSocketConnection connection) throws Exception {
        datas.onCompleted();
    }
}
