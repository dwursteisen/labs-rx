package fr.soat.labs.rx.handler;

import fr.soat.labs.rx.model.Train;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;
import rx.schedulers.Schedulers;

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
        Schedulers.computation().schedulePeriodically(() -> {
            String id = "TRAIN_ID" + random.nextInt(5);
            Train train = new Train(id);
            connections.forEach((c) -> c.send(train.serialise()));
        }, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void onOpen(WebSocketConnection connection) throws Exception {
        connections.add(connection);
        Logger.getLogger(LOG_TAG).info("Nouvelle connexion ajouté !");

    }

    @Override
    public void onClose(WebSocketConnection connection) throws Exception {
        connections.remove(connection);
        Logger.getLogger(LOG_TAG).info("Connexion fermé");
    }
}
