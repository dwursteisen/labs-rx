package fr.soat.labs.rx;

import com.github.ryenus.rop.OptionParser;
import org.webbitserver.*;
import rx.Observable;
import rx.Observer;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 22/03/14
 * Time: 15:40
 * To change this template use File | Settings | File Templates.
 */
@OptionParser.Command(name = "spawner", descriptions = "Will spawn N instances of web server. Will recreate it if servers are killed")
public class InstancesSpawner {

    @OptionParser.Option(opt = {"-i", "--instances"}, description = "number of instances to spawn")
    private int numberOfInstances = 5;
    @OptionParser.Option(opt = {"-p", "--minPort"}, description = "First port to use")
    private int minPort = 4567;

    public static void main(String[] args) {
        OptionParser parser = new OptionParser(InstancesSpawner.class);
        parser.parse(args);
    }

    public void run(OptionParser parser, String[] params) { // either or both args can be omitted


        Observable<Integer> startintPort = Observable.range(minPort, numberOfInstances);

        Observable<String> responseObserver = Observable.interval(5, TimeUnit.SECONDS).map((i) -> "MSG " + i);

        Observable<WebServer> ws = startintPort
                .flatMap((port) -> Observable.from(WebServers.createWebServer(port)
                        .add("/update/?", new EventSourceOperation(responseObserver))
                        .start()))
                .cache();

        ws.subscribe(new WSObserver());


    }

    private static class EventSourceOperation implements EventSourceHandler {

        private final Observable<String> observer;

        private Observable<EventSourceConnection> connections = Observable.empty();

        private EventSourceOperation(Observable<String> observer) {
            this.observer = observer;
            this.observer.map(EventSourceMessage::new)
                    .flatMap((msg) -> Observable.zip(connections,
                            connections.map((c) -> msg),
                            Message::new))
                    .subscribe((msg) -> msg.client.send(msg.message));
        }

        @Override
        public void onOpen(EventSourceConnection connection) throws Exception {
            this.connections = Observable.merge(connections, Observable.just(connection));
        }

        @Override
        public void onClose(EventSourceConnection connection) throws Exception {
            this.connections = connections.filter((c) -> !c.equals(connection));
        }

        private static class Message {
            private final EventSourceMessage message;
            private final EventSourceConnection client;

            private Message(EventSourceConnection client, EventSourceMessage message) {
                this.message = message;
                this.client = client;
            }
        }
    }

    private static class WSObserver implements Observer<WebServer> {

        private Logger log = Logger.getLogger("WSObserver");

        @Override
        public void onCompleted() {
            log.info("All webservers were spawned !");

        }

        @Override
        public void onError(Throwable e) {
            log.warning("Got an issue when trying to spawn a webserver");
            e.printStackTrace();
        }

        @Override
        public void onNext(WebServer o) {
            log.info("WS started : " + o.getUri());
        }
    }
}
