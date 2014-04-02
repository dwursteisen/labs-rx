package fr.soat.labs.rx;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.github.ryenus.rop.OptionParser;
import fr.soat.labs.rx.services.ComputeService;
import org.webbitserver.EventSourceConnection;
import org.webbitserver.EventSourceHandler;
import org.webbitserver.EventSourceMessage;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

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

        final ComputeService computer = new ComputeService();

        Subject<Operation, Operation> operations = PublishSubject.create();

        Observable<String> computeResponseAsStr = operations
                .flatMap((op) -> computer.compute(op.operande, op.op1, op.op2))
                .map(ComputeService.Result::getResult);

        Observable<WebServer> ws = startintPort
                .flatMap((port) -> Observable.from(WebServers.createWebServer(port)
                        .add("/compute/?", new ComputeOperation(operations))
                        .add("/update/?", new EventSourceOperation(computeResponseAsStr))
                        .start()))
                .cache();

        ws.subscribe(new WSObserver());


    }

    static class Operation {
        private final String operande;
        private final String op1;
        private final String op2;

        Operation(final String operande, final String op1, final String op2) {
            this.operande = operande;
            this.op1 = op1;
            this.op2 = op2;
        }
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

    private static class ComputeOperation implements HttpHandler {
        private final Subject<Operation, Operation> operations;

        public ComputeOperation(final Subject<Operation, Operation> operations) {
            this.operations = operations;
        }

        @Override
        public void handleHttpRequest(final HttpRequest request, final HttpResponse response, final HttpControl control) throws Exception {
            String operande = request.queryParam("operande");
            String op1 = request.queryParam("op1");
            String op2 = request.queryParam("op2");
            operations.onNext(new Operation(operande, op1, op2));
            response.content("OK").end();
        }
    }
}
