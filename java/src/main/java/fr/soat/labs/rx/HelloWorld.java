package fr.soat.labs.rx;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.webbitserver.EventSourceConnection;
import org.webbitserver.EventSourceHandler;

import org.webbitserver.EventSourceMessage;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.util.functions.Action0;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

/**
 * Created with IntelliJ IDEA. User: david.wursteisen Date: 10/02/14 Time: 15:34 To change this template use File | Settings | File
 * Templates.
 */
public class HelloWorld implements EventSourceHandler {

    private static final String LOG_TAG = "Eventsource";

    @Override
    public void onOpen(final EventSourceConnection connection) throws Exception {
        Logger.getLogger(LOG_TAG).info("Open SSE connection");

        Observable<Integer> obs = Observable.create(new Observable.OnSubscribeFunc<Integer>() {
            @Override
            public Subscription onSubscribe(final Observer<? super Integer> observer) {
                return Schedulers.currentThread().schedulePeriodically(new RandomTask(observer), 0, 2, TimeUnit.SECONDS);
            }
        });

        obs.map(new Func1<Integer, String>() {
            @Override
            public String call(final Integer randomValue) {
                return String.format("Hello number %d", randomValue);
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(final String message) {
                connection.send(new EventSourceMessage(message));
            }
        });
    }

    @Override
    public void onClose(final EventSourceConnection connection) throws Exception {
        Logger.getLogger(LOG_TAG).info("Close SSE connection");

    }

    private static class RandomTask implements Action0 {

        private final Observer<? super Integer> target;

        private final Random random = new Random();

        private RandomTask(final Observer<? super Integer> target) {
            this.target = target;
        }

        @Override
        public void call() {
            target.onNext(random.nextInt(100));
        }
    }
}
