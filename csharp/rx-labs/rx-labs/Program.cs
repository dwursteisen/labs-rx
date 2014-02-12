using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using System.Net;
using System.Reactive.Subjects;
using Fleck;
using RestSharp;

namespace rx_labs
{
    class Program
    {
        static void Main(string[] args)
        {

            var client = new RestClient("https://api.github.com");

            var reactive = new Subject<Tuple<IWebSocketConnection, string>>();
            reactive.Subscribe(tuple => {
                var connection = tuple.Item1;
                var user = tuple.Item2;

                var request = new RestRequest("users/{user}", Method.GET);
                request.AddUrlSegment("user", user);

                var response = client.Execute(request);
                connection.Send("Result -> "+response.Content);
            });

            var server = new WebSocketServer("ws://localhost:81");
            
            server.Start(socket =>
            {
                socket.OnOpen = () => Console.WriteLine("Open!");
                socket.OnClose = () => Console.WriteLine("Close!");
                socket.OnMessage = message => reactive.OnNext(new Tuple<IWebSocketConnection,string>(socket, message));
            });

            var command = string.Empty;
            while (command != "exit")
            {
                command = Console.ReadLine();
            }
            
        }

        
    }
}
