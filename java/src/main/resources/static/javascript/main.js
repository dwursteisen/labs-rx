$(document).ready(function () {
    console.log("jquery ok");

    var source = Rx.Observable.create(function (observer) {
        // https://developer.mozilla.org/en-US/docs/Server-sent_events/Using_server-sent_events
        var eventSource = new EventSource("/generators");
        eventSource.onmessage = function (event) {
            observer.onNext(event);
        };
        eventSource.onerror = function (event) {
            observer.onError(event);
        };

        eventSource.onclose = function () {
            observer.onCompleted();
        }
    });

    var buildWSObserver = function (port) {
        return Rx.Observable.create(function (observer) {
            var socket = new WebSocket("ws://127.0.0.1:" + port + "/update/");
            socket.onerror = function (event) {
                observer.onError(event);
            }

            socket.onmessage = function (message) {
                observer.onNext(message);
            }

            socket.onclose = function () {
                observer.onCompleted();
            }
        }).map(function (message) {
                return JSON.parse(message.data);
            });
    };

    var portsUpdate = source.retry().map(function (event) {
        return event.data
    }).map(function (json) {
            return JSON.parse(json);
        });


    var pricesObservables = portsUpdate.map(function (portList) {
        // return Rx.Observable.fromArray(portList).map(function (port) {
        //     console.log("port ===" + port);
        //     return buildWSObserver(port)
        // });

        // uggly !! fixme ;)
        var result = [];
        $(portList).each(function (index, elt) {
            result.push(buildWSObserver(elt));
        })
        return result;
    })

    var tableTemplate = doT.compile($("#port_line").html());
    pricesObservables.subscribe(function (listOfObservables) {
        console.log("obs -> " + listOfObservables);

        Rx.Observable.fromArray(listOfObservables).subscribe(function (obs) {
            var priceUpdate = obs.retry();
            priceUpdate.first().subscribe(function (price) {

                priceUpdate.sample(5000).map(function (price) {
                    return price.price;
                }).subscribe(function (ave) {
                        $("#average_" + price.port).html(ave);
                    });
            });

            priceUpdate.subscribe(function (priceUpdate) {
                var html = tableTemplate(priceUpdate);
                $("#row_" + priceUpdate.port).replaceWith(html);
            });
        });
    });

    var template = doT.compile($("#port_template").html());

    portsUpdate.flatMap(function (listPort) {
        return Rx.Observable.fromArray(listPort);
    }).filter(function (port) {
            return $("#row_" + port).length == 0;
        }).map(function (port) {
            return tableTemplate({price: 0, port: port, status: ""})
        }).subscribe(function (html) {
            $("#line").append(html);
        });


    portsUpdate.map(function (data) {
        return template(data)
    }).subscribe(function (html) {
            $("#port").html(html);
        });

});