package fr.soat.labs.rx.mock;

import fr.soat.labs.rx.Server;
import fr.soat.labs.rx.handler.IncidentHandler;
import fr.soat.labs.rx.model.DepartArrivee;
import fr.soat.labs.rx.model.Incident;
import fr.soat.labs.rx.model.Train;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.netty.WebSocketClient;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;
import rx.util.functions.Action0;
import rx.util.functions.Func1;
import rx.util.functions.Func2;

import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 24/02/14
 * Time: 10:01
 * To change this template use File | Settings | File Templates.
 */
public class TrainGenerator {
    private final static String LOG_TAG = "TrainGenerator";

    public static void main(String[] args) throws Exception {
        Future<? extends WebServer> ws = WebServers.createWebServer(9001) // \n
                .add("/?", new TrainGeneratorHandler())  // \n
                .start();

        Logger.getLogger(LOG_TAG).info("Running CSharp Stub server at " + ws.get().getUri());

    }

    private static class TrainGeneratorHandler extends BaseWebSocketHandler {

        private final Collection<WebSocketConnection> connections = new LinkedList<>();
        private final Observable<Train> delayed;

        private List<Train> liveTrains = new ArrayList<>();

        private Subject<Train, Train> subjets = PublishSubject.create();

        private TrainGeneratorHandler() throws Exception {
            Action0 sendDepartMessageToEveryOne = () -> {
                Train train = new Train();
                train.setId(generateId());
                train.setDepartArrivee(DepartArrivee.DEPART);
                subjets.onNext(train);
            };

            Schedulers.newThread().schedulePeriodically(sendDepartMessageToEveryOne, 0, 5, TimeUnit.SECONDS);

            delayed = subjets.delay(3, TimeUnit.SECONDS).map((t) -> t.arrivee());
            subjets.subscribe((train) -> connections.forEach(c -> c.send(train.serialise())));

            // gestion des incidents
            // ecoute du flux des incidents, à la lecture d'un incident, on vire le train de la liste
            Subject<Incident, Incident> broker = PublishSubject.create();
            Future<WebSocketClient> client = new WebSocketClient(new URI("ws://192.168.1.202"), new IncidentHandler(broker)).start();

            Collection<String> incidents = new HashSet<>();
            broker.subscribe((in) -> incidents.add(in.getId()));

            delayed.doOnNext(t ->System.out.println("DO : "+incidents))
                    .filter(t -> !incidents.contains(t.getId()))
                    .subscribe((train) -> connections.forEach(c -> c.send(train.serialise())));
        }

        private static int nbGeneratedTrain = 0;

        private String generateId() {
            synchronized (this) {
                nbGeneratedTrain++;
                System.out.println("Generated id = " + nbGeneratedTrain);
                return "" + nbGeneratedTrain;
            }
        }

        @Override
        public void onOpen(WebSocketConnection connection) throws Exception {
            System.out.println(connection);
            connections.add(connection);
            Logger.getLogger(LOG_TAG).info("Connection added : " + connection);
        }

        @Override
        public void onClose(WebSocketConnection connection) throws Exception {
            connections.remove(connection);
            Logger.getLogger(LOG_TAG).info("Connection removed : " + connection);
        }
    }
}
