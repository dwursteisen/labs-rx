package fr.soat.labs.rx;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.github.ryenus.rop.OptionParser;
import fr.soat.labs.rx.handler.WebSocketClientOperation;
import fr.soat.labs.rx.handler.WebSocketOperation;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebServers;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.netty.WebSocketClient;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@OptionParser.Command(name = "master", descriptions = "Publish all nodes URL")
public class Master {

    private final Collection<String> nodes = new ConcurrentLinkedQueue<>();
    private final Collection<String> killed = new ConcurrentLinkedQueue<>();
    private final Collection<WebSocketClient> clients = new ConcurrentLinkedQueue<>();
    @OptionParser.Option(opt = {"--port"}, description = "Instance spawner port")
    private int port = 4444;

    public static void main(String[] args) {
        OptionParser parser = new OptionParser(Master.class);
        parser.parse(args);
    }

    public void run(OptionParser parser, String[] params) throws Exception { // either or both args can be omitted


        Subject<String, String> newPorts = PublishSubject.create();
        Subject<String, String> killedPorts = PublishSubject.create();

        newPorts.subscribe(nodes::add);
        newPorts.subscribe((uri) -> System.out.println(String.format("Node at %s added to master", uri)));


        Observable<Long> ping = Observable.interval(1, TimeUnit.SECONDS);
        newPorts.map((uri) -> {
            try {
                String pingUrl = toWsUrl(uri);
                WebSocketClient client = new WebSocketClient(new URI(pingUrl), new BaseWebSocketHandler() {

                    private Subscription subscription;
                    private Subscription pingSubscription;


                    @Override
                    public void onOpen(final WebSocketConnection connection) throws Exception {
                        pingSubscription = ping.subscribe((p) -> {
                            subscription = Schedulers.computation().schedule((inner) -> {
                                killedPorts.onNext(uri);
                                stopMe();
                            }, 5, TimeUnit.SECONDS);
                            connection.send(uri);
                        });

                    }

                    private void stopMe() {
                        if (pingSubscription != null) {
                            pingSubscription.unsubscribe();
                        }
                    }

                    @Override
                    public void onMessage(final WebSocketConnection connection, final String msg) throws Throwable {
                        if (subscription != null) {
                            subscription.unsubscribe();
                        }
                    }
                });
                return client.start().get();
            } catch (URISyntaxException | InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).subscribe(clients::add);


        Observable<String> filteredKilledPorts = killedPorts.filter((uri) -> !killed.contains(uri));


        Observable.from(WebServers.createWebServer(port)
                .add("/register/?", new WebSocketClientOperation(newPorts) {
                    @Override
                    public void onClose(final WebSocketConnection connection) throws Exception {
                        // do nothing
                    }
                })
                .add("/registered/?", new WebSocketOperation(newPorts))
                .add("/killed", new WebSocketOperation(filteredKilledPorts.map((uri) -> {
                    int portIndex = uri.lastIndexOf(":");
                    int endPortIndex = uri.indexOf("/", portIndex);
                    return uri.substring(portIndex + 1, endPortIndex).trim();
                })))
                .start())
                .subscribe((ws) -> System.out.println("Master started on " + ws.getUri()));

        filteredKilledPorts.subscribe((uri) -> {
            System.out.println(String.format("%s killed in action", uri));
            killed.add(uri);
            nodes.remove(uri);
            Observable.from(clients).filter((c) -> c.getUri().toString().equals(toWsUrl(uri))).subscribe(clients::remove);
        });
    }

    private String toWsUrl(final String uri) {
        return uri.replace("http://", "ws://") + "ping/";
    }

}