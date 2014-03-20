package fr.soat.labs.rx.model;

import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by formation on 28/02/14.
 */
public class Incident {
    public String id;
    public Train train;
    public String message;
    public String status;
    public String type = "INCIDENT";

    public String hour = LocalDateTime.now().format(DateTimeFormatter.ISO_TIME);

    public Incident(String id, String status) {
        this.id = id;
        this.status = status;
    }

    public Incident() {
    }

    public Incident deserialise(String str) {
        return new Gson().fromJson(str, Incident.class);
    }

    public String serialize() {
        return new Gson().toJson(this);
    }
}
