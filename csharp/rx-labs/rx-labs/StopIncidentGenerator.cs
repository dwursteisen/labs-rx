using System;
using System.Collections.Generic;
using System.Linq;
using System.Reactive.Linq;
using System.Reactive.Subjects;
using System.Text;

namespace rx_labs
{
    public class StopIncident
    {
        public string Id { get; set; }
    }

    class StopIncidentGeneratorRandom
    {

        private readonly IObservable<Incident> _incidentStream;
        public IConnectableObservable<StopIncident> StopIncidentStream { get; private set; }

        public StopIncidentGeneratorRandom(IObservable<Incident> incidentStream)
        {
            _incidentStream = incidentStream;
            CreateObservationOnIncident();
        }

        private IObservable<string> GenerateStopIncident(Incident t)
        {
            return Observable.Interval(TimeSpan.FromSeconds(2))
                .Select(_ =>
                    {
                        var random = new Random((int)DateTime.Now.Ticks);
                        if (random.Next(0, 3) == 2)
                            return "GO";
                        else
                            return string.Empty;
                    });
        }

        private void CreateObservationOnIncident()
        {
            StopIncidentStream = (from incident in _incidentStream
                                  from status in GenerateStopIncident(incident).Where(status => status != string.Empty).Take(1)
                                select
                                new StopIncident
                                {
                                    Id = incident.Id,
                                })
                                .Publish();
        }


        public void Start()
        {
            StopIncidentStream.Connect();

        }
    }
}
