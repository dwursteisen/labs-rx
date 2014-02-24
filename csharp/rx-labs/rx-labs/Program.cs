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

           
            
            var server = new WebSocketServer("ws://localhost:9001");

            var clients = new List<IWebSocketConnection>();


            var reactive = new Subject<string>();
            reactive.Subscribe(message => {
                clients.ForEach(co => co.Send("{\"id\":"+message+", \"status\": \"OK\"}"));
            });

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
            while (command != "exit")
            {
                command = Console.ReadLine();
                if (command != "exit")
                {
                    reactive.OnNext(command);
                }
                
            }
            
        }

        
    }
}
