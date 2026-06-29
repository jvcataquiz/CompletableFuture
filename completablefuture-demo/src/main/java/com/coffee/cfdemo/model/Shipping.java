package com.coffee.cfdemo.model;

public class Shipping {

    private String shipmentId;
    private String carrier;
    private String estimatedDelivery;
    private boolean available;

    public Shipping() {
    }

    public Shipping(String shipmentId, String carrier, String estimatedDelivery, boolean available) {
        this.shipmentId = shipmentId;
        this.carrier = carrier;
        this.estimatedDelivery = estimatedDelivery;
        this.available = available;
    }

    public static Shipping unavailable() {
        return new Shipping(null, null, "UNAVAILABLE", false);
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(String estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
