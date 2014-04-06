package fr.soat.labs.rx;

import com.github.ryenus.rop.OptionParser;
import fr.soat.labs.rx.handler.WebSocketClientOperation;
import fr.soat.labs.rx.handler.WebSocketOperation;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebServers;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.netty.WebSocketClient;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

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
                .map((uri) -> uri.replace("http://", "ws://") + "ping/")
                .flatMap((uri) -> {
                    System.out.println("will ping " + uri);
                    try {
                        new WebSocketClient(new URI(uri), new BaseWebSocketHandler() {
                            @Override
                            public void onOpen(WebSocketConnection connection) throws Exception {
                                connection.send(uri);
                            }

                            @Override
                            public void onClose(WebSocketConnection connection) throws Exception {
                                throw new RuntimeException();
                            }
                        }).start().get(5, TimeUnit.SECONDS);
                        return Observable.<String>empty();
                    } catch (Exception ex) {
                        return Observable.just(uri);
                    }
                }).subscribe(killedPorts::onNext);

        killedPorts.subscribe((uri) -> {
            System.out.println(String.format("%s killed in action", uri));
            killed.add(uri);
            nodes.remove(uri);
        });

        Observable.from(WebServers.createWebServer(port)
                .add("/register/?", new WebSocketClientOperation(newPorts))
                .add("/registered/?", new WebSocketOperation(newPorts))
                .add("/killed", new WebSocketOperation(killedPorts))
                .start())
                .subscribe((ws) -> System.out.println("Master started on " + ws.getUri()));
    }
}