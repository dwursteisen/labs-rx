package fr.soat.labs.rx;

import com.github.ryenus.rop.OptionParser;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import rx.Observable;
import rx.Observer;

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

        Observable.range(minPort, numberOfInstances)
                .flatMap((port) -> Observable.from(WebServers.createWebServer(port).start()))
                .subscribe(new WSObserver());
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
