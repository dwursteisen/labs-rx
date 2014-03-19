package fr.soat.labs.rx.model;

import com.google.gson.Gson;

/**
 * Created by formation on 28/02/14.
 */
public class Incident {
    private String id;
    private String status;

    public Incident(String id, String status) {
        this.id = id;
        this.status = status;
    }

    public Incident() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String serialise() {
        return new Gson().toJson(this);
    }

    public static Incident deserialise(String json) {
        return new Gson().fromJson(json, Incident.class);
    }
}
