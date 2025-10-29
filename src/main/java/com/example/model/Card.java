package com.example.model;

public class Card {
    public enum CardType {
        ROOM,
        SUSPECT,
        WEAPON
    }

    private String name;
    private CardType type;

    public Card(String name, CardType type) {
        this.name = name;
        this.type = type;
    }
    public CardType getType(){return type;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public void setType(CardType type) {this.type = type;}
    @Override
    public String toString(){return name+" ( "+type+")";}
}