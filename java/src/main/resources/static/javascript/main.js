$(document).ready(function() {
    console.log("jquery ok");

    var source = Rx.Observable.create(function(observer) {
        // https://developer.mozilla.org/en-US/docs/Server-sent_events/Using_server-sent_events
        var eventSource = new EventSource("/update");
        eventSource.onmessage = function(event) {
            observer.onNext(event);
        };
        eventSource.onerror = function(event) {
            observer.onError(event);
        };
    });

    var template = doT.compile($("#message").html());
    source.map(function(event) {return event.data})
          .map(function(data) {return template(data)})
          .subscribe(function(html) {
            $("#timeline").append(html);
        });
});