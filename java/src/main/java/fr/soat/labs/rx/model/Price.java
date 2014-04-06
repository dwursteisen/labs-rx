package fr.soat.labs.rx.model;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 05/04/2014
 * Time: 01:41
 * To change this template use File | Settings | File Templates.
 */
public class Price {

    private int price;
    private String node;
    private Indice indice;

    public Price() {
    }

    public Price(int price, Indice indice) {
        this.price = price;
        this.indice = indice;
    }

    public Price(int price, String node, Indice indice) {
        this.price = price;
        this.node = node;
        this.indice = indice;
    }

    public String getNode() {
        return node;
    }

    public Indice getIndice() {
        return indice;
    }

    public int getPrice() {
        return price;
    }

    public static enum Indice {
        EUR_3M,
        EUR_6M,
        EUR_9M,
        LIBOR_3M,
        LIBOR_6M,
        LIBOR_9M

    }
}
