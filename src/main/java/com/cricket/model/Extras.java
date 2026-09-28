package com.cricket.model;

public class Extras {
    private int wides;
    private int noBalls;
    private int byes;
    private int legByes;
    private int penalty;

    public Extras() {}

    public Extras(int wides, int noBalls, int byes, int legByes, int penalty) {
        this.wides = wides;
        this.noBalls = noBalls;
        this.byes = byes;
        this.legByes = legByes;
        this.penalty = penalty;
    }

    public int getTotal() {
        return wides + noBalls + byes + legByes + penalty;
    }

    public int getWides() {
        return wides;
    }

    public void setWides(int wides) {
        this.wides = wides;
    }

    public int getNoBalls() {
        return noBalls;
    }

    public void setNoBalls(int noBalls) {
        this.noBalls = noBalls;
    }

    public int getByes() {
        return byes;
    }

    public void setByes(int byes) {
        this.byes = byes;
    }

    public int getLegByes() {
        return legByes;
    }

    public void setLegByes(int legByes) {
        this.legByes = legByes;
    }

    public int getPenalty() {
        return penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }
}
