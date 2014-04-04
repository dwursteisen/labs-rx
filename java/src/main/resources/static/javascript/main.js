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
    });

    var portsUpdate = source.retry().map(function (event) {
        return event.data
    }).map(function (json) {
            return JSON.parse(json);
        });

    var template = doT.compile($("#port_template").html());

    portsUpdate.map(function (data) {
        return template(data)
    }).subscribe(function (html) {
         $("#port").html(html);
    });
});