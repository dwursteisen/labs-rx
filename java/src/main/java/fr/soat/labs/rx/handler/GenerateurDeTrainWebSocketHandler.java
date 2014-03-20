package fr.soat.labs.rx.handler;

import fr.soat.labs.rx.model.Train;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;
import rx.Observable;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by formation on 28/02/14.
 */
public class GenerateurDeTrainWebSocketHandler extends BaseWebSocketHandler {


    private final static String LOG_TAG = "GenerateurDeTrainHandler";

    private final Collection<WebSocketConnection> connections = new LinkedList<>();

    private final SecureRandom random = new SecureRandom();

    public GenerateurDeTrainWebSocketHandler() {
        Observable.interval(5, TimeUnit.SECONDS)
                .map((l) -> "TRAIN_ID" + random.nextInt(5))
                .map(Train::new)
                .map(Train::serialise)
                .doOnEach((json) -> log("Json généré : "+json))
                .subscribe((json) -> connections.forEach(c -> c.send(json)));
    }

    @Override
    public void onOpen(WebSocketConnection connection) throws Exception {
        connections.add(connection);
        log("Nouvelle connexion ajouté !");

    }

    private void log(String msg) {
        Logger.getLogger(LOG_TAG).info(msg);
    }

    @Override
    public void onClose(WebSocketConnection connection) throws Exception {
        connections.remove(connection);
        log("Connexion fermé");
    }
}
