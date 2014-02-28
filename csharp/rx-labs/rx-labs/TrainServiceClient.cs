using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using System.Net;

using System.Reactive.Linq;
using WebSocketSharp;

namespace rx_labs
{
    class TrainServiceClient
    {
        public IObservable<Train> TrainStream { get; private set; }
        private readonly string _endpoint;
        private readonly WebSocket _server; 

        public TrainServiceClient(string endpoint)
        {
            _endpoint = endpoint;
            _server = new WebSocket(_endpoint);  
            CreateObservationOnTrainServiceClient();
        }

        private void CreateObservationOnTrainServiceClient()
        {

            TrainStream = Observable.Create<Train>(obs =>
            {
                var subscription = Observable.FromEventPattern<MessageEventArgs>(_server, "OnMessage")
                    .Select(m =>  Newtonsoft.Json.JsonConvert.DeserializeObject <Train> (m.EventArgs.Data))
                    .Subscribe(obs);


                return () => {  subscription.Dispose(); };
            }).Publish().RefCount();

            _server.Connect();

        }

        public void PushMessage(string message)
        {
            _server.Send(message);
        }


        public object Train { get; set; }
    }
}
