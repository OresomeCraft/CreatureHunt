package com.oresomecraft.creaturehunt;

public class GameStorage {

    private short score;
    private double pot;
    private short deaths;
    
    public GameStorage() {
        score = 0;
        pot = 0.0;
        deaths = 0;
    }
    
    public short getScore() {
        return score;
    }
    
    public double getPot() {
        return pot;
    }
    
    public short getDeaths() {
        return deaths;
    }
    
    public void incrementScore(short points) {
        score += points;
    }
    
    public void setScore(short score) {
        this.score = score;
    }
    
    public void incrementPot(double money) {
        pot += money;
    }
    
    public void setPot(double pot) {
        this.pot = pot;
    }
    
    public void deathPenalty() {
        pot = pot / 2;
        deaths += 1;
    }
}
