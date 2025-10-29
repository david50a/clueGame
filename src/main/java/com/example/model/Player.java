package com.example.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Player {
    private String name;
    protected List<Card>hand=new ArrayList<>();
    protected boolean eliminated=false;
    public Player(String name){this.name=name;}
    public void addCard(Card c){hand.add(c);}
    public boolean hasCard(Card c){return hand.contains(c);}
    public String getName() {return name;}
    public void showHand(){System.out.println(name+"'s cards: "+hand);}
    public List<Card> getHand() {return hand;}
    public boolean isEliminated(){return eliminated;}
    public void setEliminated(boolean e){eliminated=e;}
    public Optional<Card> refute(Card suspect,Card weapon,Card room){
        for(Card c:hand){
            if(c.equals(suspect)||c.equals(weapon)||c.equals(room)){return Optional.of(c);}
        }return Optional.empty();
    }
}
