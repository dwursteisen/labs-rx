package fr.soat.labs.rx.model;

import com.google.gson.Gson;

/**
 * Created by formation on 28/02/14.
 */
public class Train {

    public String id;

    public String type = "TRAIN";

    public DepartArrivee departArrivee;
    
    public Train() {
    }

    public Train(String id) {
        this.id = id;
    }

    public String serialise() {
        return new Gson().toJson(this);
    }

    public Train deserialise(String json) {
        return new Gson().fromJson(json, Train.class);
    }


    public Train arrivee() {
        Train t = deserialise(serialise());
        t.departArrivee = DepartArrivee.ARRIVEE;
        return t;
    }

    public String toString() {
        return serialise();
    }
}
