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
            var serviceClient = new TrainServiceClient("ws://192.168.1.203:9001");

            //var trainStream = new Subject<Train> (); //Lire socket et recuperer les events sur le train
            var incidentManager = new IncidentGeneratorRandom(serviceClient.TrainStream);
            var stopIncidentManager = new StopIncidentGeneratorRandom(incidentManager.IncidentStream);

            //IncidentContributor server = new IncidentContributor(string.Empty);
            //server.Start();


            incidentManager.IncidentStream.Subscribe(incident =>
                {
                    string msg = string.Format("{{ 'id':'{0}', 'status':'{1}' }}", incident.Id, incident.Message);
                    //Console.WriteLine();
                  
                    //Push les events de train sur socket
                   // server.Push(msg);
                    serviceClient.PushMessage(msg);
                });

            stopIncidentManager.StopIncidentStream.Subscribe(stopIncident =>
            {
                string msg = DateTime.Now + " Incident :" + stopIncident.Id + " =========> Terminé";
                //Console.WriteLine();
                
                //Push les events de train sur socket
                //server.Push(msg);
            });

            incidentManager.Start();
            stopIncidentManager.Start();

            //for (int i = 1; i < 20000; i++)
            //    trainStream.OnNext(new Train { Id = i.ToString() });


            //Console.WriteLine("Generer inc
            Console.ReadLine();
        }

        
    }
}
