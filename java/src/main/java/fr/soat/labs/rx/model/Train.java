package fr.soat.labs.rx.model;

import com.google.gson.Gson;

/**
 * Created by formation on 28/02/14.
 */
public class Train {

    private String id;

    private DepartArrivee departArrivee;

    public String getId() {
        return id;
    }

    public Train() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public DepartArrivee getDepartArrivee() {
        return departArrivee;
    }

    public void setDepartArrivee(DepartArrivee departArrivee) {
        this.departArrivee = departArrivee;
    }

    public String serialise() {
        return new Gson().toJson(this);
    }

    public static Train deserialise(String json) {
        return new Gson().fromJson(json, Train.class);
    }

    public Train arrivee() {
        Train t = deserialise(serialise());
        t.setDepartArrivee(DepartArrivee.ARRIVEE);
        return t;
    }

    public String toString() {
        return serialise();
    }
}
