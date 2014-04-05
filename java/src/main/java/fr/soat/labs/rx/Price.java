package fr.soat.labs.rx;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 05/04/2014
 * Time: 01:41
 * To change this template use File | Settings | File Templates.
 */
public class Price {

    private int price;
    private int port;
    private String status = "OK";

    public Price() {
    }

    public Price(final int price, final int port) {
        this.price = price;
        this.port = port;
    }

    public int getPrice() {
        return price;
    }

    public int getPort() {
        return port;
    }

    public String getStatus() {
        return status;
    }
}
