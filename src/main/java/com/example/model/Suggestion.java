package com.example.model;

public class Suggestion {
    private Card suspect,weapon,room;
    public Suggestion(Card suspect,Card weapon, Card room){
        this.suspect=suspect;
        this.room=room;
        this.weapon=weapon;
    }
    public Card getSuspect() {return suspect;}
    public Card getWeapon() {return weapon;}
    public Card getRoom() {return room;}
    @Override
    public String toString(){return suspect+" with the "+weapon+" in the "+room;}
}
