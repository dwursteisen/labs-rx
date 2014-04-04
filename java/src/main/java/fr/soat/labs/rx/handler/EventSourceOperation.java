package fr.soat.labs.rx.handler;

import java.util.HashMap;
import java.util.Map;

import org.webbitserver.EventSourceConnection;
import org.webbitserver.EventSourceHandler;
import org.webbitserver.EventSourceMessage;
import rx.Observable;
import rx.Subscription;

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
public class EventSourceOperation implements EventSourceHandler {

    private final Observable<String> updates;

    private final Map<EventSourceConnection, Subscription> subscriptions = new HashMap<>();


    public EventSourceOperation(final Observable<String> observer) {
        this.updates = observer;
    }

    @Override
    public void onOpen(final EventSourceConnection connection) throws Exception {
        Subscription subscription = updates.subscribe((msg) -> connection.send(new EventSourceMessage(msg)));
        subscriptions.put(connection, subscription);
    }

    @Override
    public void onClose(final EventSourceConnection connection) throws Exception {
        Subscription subscription = subscriptions.get(connection);
        if(subscription != null) {
            subscription.unsubscribe();
        }
    }

}
