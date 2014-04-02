package fr.soat.labs.rx.services;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
public class ComputeService {

    public Observable<Result> compute(final String operator, final String operande1, final String operande2) {
        Observable<Result> obs;
        switch (operator) {
            case "plus":
                obs =  sum(Integer.parseInt(operande1), Integer.parseInt(operande2));
                break;
            case "moins":
                obs = minus(Integer.parseInt(operande1), Integer.parseInt(operande2));
                break;
            default:
                throw new IllegalArgumentException(String.format("operande %s not implemented", operator));
        }

        return obs.subscribeOn(Schedulers.computation());
    }

    private Observable<Result> sum(final int operande1, final int operande2) {
        return Observable.create(new Observable.OnSubscribe<Result>() {
            @Override
            public void call(final Subscriber<? super Result> subscriber) {
                Result result = new Result("+", operande1, operande2, operande1 + operande2);
                subscriber.onNext(result);
            }
        });
    }

    private Observable<Result> minus(final int operande1, final int operande2) {
        return Observable.create(new Observable.OnSubscribe<Result>() {
            @Override
            public void call(final Subscriber<? super Result> subscriber) {
                Result result = new Result("-", operande1, operande2, operande1 - operande2);
                subscriber.onNext(result);
            }
        });
    }

    public static class Result {
        private final String operator;
        private final String operande1;
        private final String operande2;
        private final String result;

        public Result(final String operator, final int operande1, final int operande2, final int result) {
            this.operator = operator;
            this.operande1 = Integer.toString(operande1);
            this.operande2 = Integer.toString(operande2);
            this.result = Integer.toString(result);
        }

        public String getResult() {
            return result;
        }
    }

}
