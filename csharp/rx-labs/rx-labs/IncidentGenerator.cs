using System;
using System.Collections.Generic;
using System.Linq;
using System.Reactive.Linq;
using System.Reactive.Subjects;
using System.Text;

namespace rx_labs
{
    
    public enum TrainState
    {
        OK,
        KO
    }

    public class Train
    {
        public string id { get; set; }
        public string departArrivee { get; set; }
    }

    public class Incident
    {
        public string Id { get; set; }
        public string Message
        {
            get;
            set;

        }
    }
        class IncidentGeneratorRandom
        {
            private IList<string> _incidentsMessages = new List<string>
        {
                "Malaise voyageur",
                "Panne technique",
                "Greve Conducteurs",
                "Chat ecrasé",
                "Vol de cuivre",
                "Mamie clamsé",
                "Attentat",
                "SoStrike"
        };

            private readonly IObservable<Train> _trainStream;
            public IConnectableObservable<Incident> IncidentStream { get; private set; }

            public IncidentGeneratorRandom(IObservable<Train> trainStream)
            {
                _trainStream = trainStream;
                CreateObservationOnTrains();
            }

            private IObservable<string> Random(Train train)
            {
               
                return Observable.Interval(TimeSpan.FromSeconds(2))
                    .Select(_ =>
                        {
                            var random = new Random((int)DateTime.Now.Ticks);
                            
                            if (random.Next(0, 3) == 2)
                                return _incidentsMessages[random.Next(0, _incidentsMessages.Count())];
                            else
                                return string.Empty;
                        });
            }
                       

            private void CreateObservationOnTrains()
            {
                IncidentStream = (from train in _trainStream
                                  from status in Random(train).Where(status => status != string.Empty).Take(1)
                                  select 
                                  new Incident
                                  {
                                      Id = train.id,
                                      Message = status
                                  }).Publish();
            }

            public void Start()
            {
                IncidentStream.Connect();

            }
        }
}
