package fr.soat.labs.rx.operator;

/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.Observable.Operator;
import rx.Scheduler.Inner;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class OperatorRetryWithDelay<T> implements Operator<T, Observable<T>> {

    private static final int INFINITE_RETRY = -1;

    private final int retryCount;
    private final long delay;
    private final TimeUnit unit;

    public OperatorRetryWithDelay(int retryCount, long delay, TimeUnit unit) {
        this.retryCount = retryCount;
        this.delay = delay;
        this.unit = unit;
    }

    public OperatorRetryWithDelay(long delay, TimeUnit unit) {
        this(INFINITE_RETRY, delay, unit);
    }

    @Override
    public Subscriber<? super Observable<T>> call(final Subscriber<? super T> s) {
        return new Subscriber<Observable<T>>(s) {
            final AtomicInteger attempts = new AtomicInteger(0);

            @Override
            public void onCompleted() {
                // ignore as we expect a single nested Observable<T>
            }

            @Override
            public void onError(Throwable e) {
                s.onError(e);
            }

            @Override
            public void onNext(final Observable<T> o) {
                Schedulers.trampoline().schedule(new Action1<Inner>() {

                    @Override
                    public void call(final Inner inner) {
                        final Action1<Inner> _self = this;
                        attempts.incrementAndGet();
                        o.subscribe(new Subscriber<T>(s) {

                            @Override
                            public void onCompleted() {
                                s.onCompleted();
                            }

                            @Override
                            public void onError(Throwable e) {
                                if ((retryCount == INFINITE_RETRY || attempts.get() <= retryCount) && !inner.isUnsubscribed()) {
                                    // retry again
                                    inner.schedule(_self, delay, unit);
                                } else {
                                    // give up and pass the failure
                                    s.onError(e);
                                }
                            }

                            @Override
                            public void onNext(T v) {
                                s.onNext(v);
                            }

                        });
                    }
                });
            }

        };
    }
}