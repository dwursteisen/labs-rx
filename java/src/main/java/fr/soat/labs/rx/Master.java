package fr.soat.labs.rx;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.ryenus.rop.OptionParser;
import fr.soat.labs.rx.handler.WebSocketClientOperation;
import fr.soat.labs.rx.handler.WebSocketOperation;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebServers;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.netty.WebSocketClient;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@OptionParser.Command(name = "master", descriptions = "Publish all nodes URL")
public class Master {

    private final Collection<String> nodes = new ConcurrentLinkedQueue<>();
    private final Collection<String> killed = new ConcurrentLinkedQueue<>();
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

        Observable.interval(1, TimeUnit.SECONDS)
                .flatMap((i) -> Observable.from(nodes))
                .flatMap((uri) -> {
                    String ping = uri.replace("http://", "ws://") + "ping/";
                    System.out.println("Ping " + ping);
                    return Observable.create(new WebSocketPingPongOperation(ping))
                            .flatMap((result) -> Observable.<String>empty())
                            .onErrorResumeNext(Observable.just(uri));
                }
                ).subscribe(killedPorts::onNext);


        killedPorts.subscribe((uri) -> {
            System.out.println(String.format("%s killed in action", uri));
            killed.add(uri);
            nodes.remove(uri);
        });

        Observable.from(WebServers.createWebServer(port)
                .add("/register/?", new WebSocketClientOperation(newPorts) {
                    @Override
                    public void onClose(final WebSocketConnection connection) throws Exception {
                        // do nothing
                    }
                })
                .add("/registered/?", new WebSocketOperation(newPorts))
                .add("/killed", new WebSocketOperation(killedPorts))
                .start())
                .subscribe((ws) -> System.out.println("Master started on " + ws.getUri()));
    }

    private static class WebSocketPingPongOperation implements Observable.OnSubscribe<String> {
        private final String uri;

        public WebSocketPingPongOperation(final String uri) {
            this.uri = uri;
        }

        @Override
        public void call(final Subscriber<? super String> subscriber) {
            try {
                final WebSocketClient client = new WebSocketClient(new URI(uri), new BaseWebSocketHandler() {

                    private final AtomicBoolean hasRepond = new AtomicBoolean(false);

                    @Override
                    public void onOpen(final WebSocketConnection connection) throws Exception {
                        connection.send(uri);
                        Schedulers.io().schedule((schedule) -> {
                            if (!hasRepond.get()) {
                                subscriber.onError(new TimeoutException());
                            }
                        }, 5, TimeUnit.SECONDS);
                    }

                    @Override
                    public void onMessage(final WebSocketConnection connection, final String msg) throws Throwable {
                        hasRepond.set(true);
                        subscriber.onNext(msg);
                        connection.close();
                    }

                });

                Future<WebSocketClient> clientFuture = client.start();

                subscriber.add(new Subscription() {
                    @Override
                    public void unsubscribe() {
                        clientFuture.cancel(false);
                    }

                    @Override
                    public boolean isUnsubscribed() {
                        return clientFuture.isCancelled() || clientFuture.isDone();
                    }
                });
            } catch (URISyntaxException ex) {
                subscriber.onError(ex);
            }
        }
    }
}