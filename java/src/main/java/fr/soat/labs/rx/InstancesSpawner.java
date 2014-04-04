package fr.soat.labs.rx;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.github.ryenus.rop.OptionParser;
import com.google.gson.Gson;
import fr.soat.labs.rx.handler.EventSourceOperation;
import fr.soat.labs.rx.handler.WebSocketOperation;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.handler.StaticFileHandler;
import rx.Observable;
import rx.Observer;

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
    @OptionParser.Option(opt = {"-m", "--masterPort"}, description = "Master port")
    private int masterPort = 4444;

    public static void main(String[] args) {
        OptionParser parser = new OptionParser(InstancesSpawner.class);
        parser.parse(args);
    }

    public void run(OptionParser parser, String[] params) throws Exception { // either or both args can be omitted


        Observable<Integer> startintPort = Observable.range(minPort, numberOfInstances);


        Observable<WebServer> pricers = startintPort
                .flatMap((port) -> {

                    Observable<String> randomizer = Observable.interval(1, TimeUnit.NANOSECONDS).filter((i) -> new Random().nextInt(10) < 9)
                            .map((i) -> new Random().nextInt(1000)).map(String::valueOf).map((str) -> String.format("{\"price\": \"%s\"}", str));

                    return Observable.from(WebServers.createWebServer(port)
                            .add("/update/?", new WebSocketOperation(randomizer))
                            .start());
                })
                .cache();

        File staticDirectory = new File(InstancesSpawner.class.getResource("/static/").toURI());
        Observable<String> jsonPorts = startintPort.map(String::valueOf).reduce(new LinkedList<String>(), (List<String> seed, String value) -> {
            List<String> result = new LinkedList<>(seed);
            result.add(value);
            return result;
        }).flatMap((list) -> Observable.just(new Gson().toJson(list)));

        Observable<WebServer> master = Observable.from(
                WebServers.createWebServer(masterPort)
                        .add("/generators/?", new EventSourceOperation(jsonPorts.cache()))
                        .add(new StaticFileHandler(staticDirectory)).start());

        Observable.merge(master, pricers).subscribe(new WSObserver());

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

        }
    }

}
