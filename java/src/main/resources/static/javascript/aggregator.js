$(document).ready(function(){
    console.log("jQuery OK")

 var source = Rx.Observable.create(function(observer) {
         // https://developer.mozilla.org/en-US/docs/Server-sent_events/Using_server-sent_events
         var eventSource = new EventSource("/aggregator");
         eventSource.onmessage = function(event) {
             console.log("Receiving events "  + event.data);
             observer.onNext(event);
         };
         eventSource.onerror = function(event) {
             observer.onError(event);
         };
     });
     /* .distinct(function(x){
        var msg = JSON.parse(x.data);
        console.log("Distinct msg=" + msg);
     return x.data; });
     */

     var template = doT.compile($("#message").html());
/*         source.map(function(event) {return event.data})
               .map(function(data) { return JSON.parse(data); })
               .map(function(data) {return template(data)})
               .subscribe(function(html) {
                 $("#timeline").append(html);
             });*/
         source
            .map(function(event) { return JSON.parse(event.data); })
            .subscribe(function(data) {
                // Créer message s'il n'y en a pas
                if (!data.message){
                   data.message = "A l'heure";
                }
                //data.hour = new Date();

                var html = template(data);

                // Remove existing row
                var tr_row = $("#row_" + data.id);
                if(tr_row.length) {
                    tr_row.remove();
                }
                $("#timeline").append(html);
                /*
                var td_type = $("#type_" + data.id);
                var td_message = $("#msg_"+data.id);

                if(tr_row.length) {
                    $(td_type).html(data.type);
                } else {
                    if (!data.message) {
                        data.message = "A l'heure !!!";
                    }
                    var html = template(data);
                    $("#timeline").append(html);
                }
                */
             });
});


