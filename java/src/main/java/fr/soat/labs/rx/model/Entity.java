package fr.soat.labs.rx.model;

import com.google.gson.Gson;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 24/02/14
 * Time: 09:59
 * To change this template use File | Settings | File Templates.
 */
public class Entity {

    private String id;
    private Status status;

    public Entity(String id, Status status) {
        this.id = id;
        this.status = status;
    }

    public Entity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String serialise() {
        return new Gson().toJson(this);
    }

    public Entity deserialise(String json) {
        return new Gson().fromJson(json, Entity.class);
    }

    public static enum Status {
        OK, KO
    }
}



