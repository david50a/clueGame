package com.example.model;
import java.util.Random;
public class AIPlayer extends Player{
    private final Random rnd=new Random();
    public AIPlayer(String name){super(name);}
    public Suggestion makeRandomSuggestion(Game game) {
        Card person = game.getRandomSuspect();
        Card weapon = game.getRandomWeapon();
        Card room = game.getRandomRoom();
        return new Suggestion(person, weapon, room);
    }
    public boolean wantsToAccuse(){return rnd.nextInt(20)==0;}
}
