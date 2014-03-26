package fr.soat.labs.rx;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.github.ryenus.rop.OptionParser;
import fr.soat.labs.rx.operator.OperatorRetryWithDelay;
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

    private final Logger killerLog = Logger.getLogger("killer");
    private final Logger log = Logger.getLogger("log");
    @OptionParser.Option(opt = {"-i", "--instances"}, description = "number of instances to spawn")
    private int numberOfInstances = 5;
    @OptionParser.Option(opt = {"-p", "--minPort"}, description = "First port to use")
    private int minPort = 4567;
    @OptionParser.Option(opt = {"-k", "--killer"}, description = "Port used by the killer webserver")
    private int killerPort = 4444;
    private Map<Integer, WebServer> webServers = new HashMap<>();

    public static void main(String[] args) {
        OptionParser parser = new OptionParser(InstancesSpawner.class);
        parser.parse(args);
    }

    public void run(OptionParser parser, String[] params) { // either or both args can be omitted


        Subject<Integer, Integer> newPortToSpawn = PublishSubject.create();

        Observable<Integer> startintPort = Observable.range(minPort, numberOfInstances);

        Observable<WebServer> ws = Observable.merge(newPortToSpawn.delay(1, TimeUnit.SECONDS).retry(), startintPort)
                .flatMap((expectedPort) -> Observable.merge(Observable.from(expectedPort), Observable.from(webServers.keySet())).max(Comparator.naturalOrder()).map((p) -> p+1))
                .flatMap((port) -> from(WebServers.createWebServer(port))).cache();

        ws.subscribe(new WSObserver());


        ws.scan(webServers, (map, server) -> {
            HashMap<Integer, WebServer> newMap = new HashMap<>(map);
            newMap.put(server.getPort(), server);
            return newMap;
        }).subscribe((webServersMap) -> {
            webServers = webServersMap;
        });

        Subject<Integer, Integer> killed = PublishSubject.create();

        killed.doOnNext((port) -> killerLog.warning("Will try to kill " + port))
                .map(webServers::get)
                .filter((server) -> server != null)
                .flatMap((server) -> Observable.from(server.stop()))
                .subscribe(((killedServer) -> {
                    killerLog.warning("Just killed " + killedServer.getPort());
                    webServers.remove(killedServer.getPort());
                    newPortToSpawn.onNext(killedServer.getPort());
                }));


        Observable.from(WebServers.createWebServer(killerPort)
                .add("/kill/?", new KillerHandler(killed))
                .start())
                .subscribe((server) -> killerLog.info("Ready to kill at " + server.getUri()));
    }

    public Observable<WebServer> from(WebServer webserver) {
        log.info("Will try to spawn " + webserver.getPort());
        return Observable.from(webserver.start())
                .doOnError(Throwable::printStackTrace)
                .nest().lift(new OperatorRetryWithDelay<>(5, 5, TimeUnit.SECONDS));
    }

    private static class KillerHandler implements HttpHandler {

        private final Observer<Integer> observer;

        private KillerHandler(Observer<Integer> observer) {
            this.observer = observer;
        }

        @Override
        public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
            Observable.from(request.queryParam("port"))
                    .filter((p) -> p != null)
                    .map(Integer::parseInt)
                    .subscribe((port) -> {
                        observer.onNext(port);
                        response.content("OK").end();
                    }, (ex) -> {
                        ex.printStackTrace();
                        response.content("KO").end();
                    });
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
