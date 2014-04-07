package fr.soat.labs.rx;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.github.ryenus.rop.OptionParser;
import com.google.gson.Gson;
import fr.soat.labs.rx.handler.WebSocketOperation;
import fr.soat.labs.rx.model.Price;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.netty.WebSocketClient;
import rx.Observable;

@OptionParser.Command(name = "node", descriptions = "Start a node which publish prices")
public class Node {

    @OptionParser.Option(opt = {"-p", "--port"}, description = "Port Used by the node")
    private int port = 4567;
    @OptionParser.Option(opt = {"-m", "--master"}, description = "WebSocket URL to register this node")
    private String registerUrl = "ws://localhost:4444/register";
    @OptionParser.Option(opt = {"--gui"}, description = "Run a GUI(Not Yet Implemented)")
    private boolean gui = false;
    @OptionParser.Option(opt = {"--indice"}, description = "Indice to publish")
    private Price.Indice indice = Price.Indice.EUR_6M;

    public static void main(String[] args) {
        OptionParser parser = new OptionParser(Node.class);
        parser.parse(args);
    }

    public void run(OptionParser parser, String[] params) {
        final Gson gson = new Gson();
        final Random random = new Random();

        Observable<String> prices = Observable.interval(100, TimeUnit.MILLISECONDS)
                .map((i) -> random.nextInt(1000))
                .map((p) -> new Price(p, indice))
                .map(gson::toJson);

        Observable<WebServer> ws = Observable.from(WebServers.createWebServer(port)
                .add("/price/?", new WebSocketOperation(prices))
                .add("/ping/?", new BaseWebSocketHandler() {

                    @Override
                    public void onMessage(final WebSocketConnection connection, final String msg) throws Throwable {
                        connection.send(msg);
                    }

                }).start()).first().cache();

        ws.map((w) -> w.getUri().toString()).map((uri) -> {
            try {
                return new WebSocketClient(new URI(registerUrl), new BaseWebSocketHandler() {
                    @Override
                    public void onOpen(WebSocketConnection connection) throws Exception {
                        connection.send(uri);
                        connection.close();
                    }
                });
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }).flatMap((client) -> Observable.from(client.start())).subscribe();


        ws.subscribe((server) -> Logger.getLogger("Node" + port).info(server.getUri() + " started"));
    }
}
