package fr.soat.labs.rx.handler;

import fr.soat.labs.rx.InstancesSpawner;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;
import rx.Observable;
import rx.functions.Action1;

/**
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* <p/>
* http://www.apache.org/licenses/LICENSE-2.0
* <p/>
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
public class WebSocketOperation extends BaseWebSocketHandler {

    private Observable<WebSocketConnection> connections = Observable.empty();

    public WebSocketOperation(Observable<String> observer) {
        observer.flatMap((msg) -> Observable.zip(connections,
                connections.map((c) -> msg),
                Message::new))
                .subscribe((Action1<Message>) message -> {
                    message.client.send(message.message);
                });
    }

    @Override
    public void onOpen(WebSocketConnection connection) throws Exception {
        this.connections = Observable.merge(connections, Observable.just(connection));
    }

    @Override
    public void onClose(WebSocketConnection connection) throws Exception {
        this.connections = connections.filter((c) -> !c.equals(connection));
    }

    private static class Message {
        private final String message;
        private final WebSocketConnection client;

        private Message(WebSocketConnection client, String message) {
            this.message = message;
            this.client = client;
        }
    }
}
