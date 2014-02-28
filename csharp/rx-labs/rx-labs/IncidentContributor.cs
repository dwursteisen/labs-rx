using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;
using Fleck;
using WebSocketSharp;

namespace rx_labs
{
    class IncidentContributor
    {
        private List<IWebSocketConnection> clients;
        private readonly string _endpoint;
        private readonly WebSocket _server;
        public IncidentContributor(string endpoint)
        {
            _endpoint = endpoint;
            _server = new WebSocket(_endpoint);   
        }

        public void Start()
        {
            //_server.Connect();
            //server.
            var server = new WebSocketServer("ws://localhost:9001");
            clients = new List<IWebSocketConnection>();

            server.Start(socket =>
            {
                socket.OnOpen = () =>
                {
                    Console.WriteLine("Open!");
                    clients.Add(socket);
                };
                socket.OnClose = () =>
                {
                    clients.Remove(socket);
                    Console.WriteLine("Close!");
                };
                socket.OnMessage = message => Console.WriteLine("Received : " + message);
            });

            var command = string.Empty;
          
        }

        public void Push(string msg)
        {
            //_server.Send(msg);
            
            if (clients != null && clients.Count > 0)
                clients.ForEach(sc => sc.Send(msg));
        }
    }
}
