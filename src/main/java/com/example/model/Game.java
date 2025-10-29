package com.example.model;
import java.util.*;
import com.example.model.Card;
public class Game {
    private final List<Card> suspectCards = new ArrayList<>();
    private final List<Card> weaponCards = new ArrayList<>();
    private final List<Card> roomCards = new ArrayList<>();
    private final List<Player> players = new ArrayList<>();
    private final Map<Card.CardType, Card> solution = new EnumMap<>(Card.CardType.class);
    private int currentTurnIndex=0;
    public Game() {
        initCards();
    }

    private void initCards() {
        // suspects
        suspectCards.add(new Card("Miss Scarlet", Card.CardType.SUSPECT));
        suspectCards.add(new Card("Colonel Mustard", Card.CardType.SUSPECT));
        suspectCards.add(new Card("Mrs. White", Card.CardType.SUSPECT));
        suspectCards.add(new Card("Mr. Green", Card.CardType.SUSPECT));
        suspectCards.add(new Card("Mrs. Peacock", Card.CardType.SUSPECT));
        suspectCards.add(new Card("Professor Plum", Card.CardType.SUSPECT));

        // weapons
        weaponCards.add(new Card("Knife", Card.CardType.WEAPON));
        weaponCards.add(new Card("Candlestick", Card.CardType.WEAPON));
        weaponCards.add(new Card("Revolver", Card.CardType.WEAPON));
        weaponCards.add(new Card("Rope", Card.CardType.WEAPON));
        weaponCards.add(new Card("Lead Pipe", Card.CardType.WEAPON));
        weaponCards.add(new Card("Wrench", Card.CardType.WEAPON));

        // rooms
        roomCards.add(new Card("Kitchen", Card.CardType.ROOM));
        roomCards.add(new Card("Ballroom", Card.CardType.ROOM));
        roomCards.add(new Card("Conservatory", Card.CardType.ROOM));
        roomCards.add(new Card("Dining Room", Card.CardType.ROOM));
        roomCards.add(new Card("Lounge", Card.CardType.ROOM));
        roomCards.add(new Card("Hall", Card.CardType.ROOM));
        roomCards.add(new Card("Study", Card.CardType.ROOM));
        roomCards.add(new Card("Library", Card.CardType.ROOM));
        roomCards.add(new Card("Billiard Room", Card.CardType.ROOM));
    }

    public void addPlayer(Player p) { players.add(p); }
    public List<Player> getPlayers() { return players; }
    public List<Card> getSuspectCards() { return suspectCards; }
    public List<Card> getWeaponCards() { return weaponCards; }
    public List<Card> getRoomCards() { return roomCards; }

    public void setupAndDeal() {
        Random rnd = new Random();

        Card solSuspect = suspectCards.remove(rnd.nextInt(suspectCards.size()));
        Card solWeapon  = weaponCards.remove(rnd.nextInt(weaponCards.size()));
        Card solRoom    = roomCards.remove(rnd.nextInt(roomCards.size()));

        solution.put(Card.CardType.SUSPECT, solSuspect);
        solution.put(Card.CardType.WEAPON, solWeapon);
        solution.put(Card.CardType.ROOM, solRoom);

        List<Card> deck = new ArrayList<>();
        deck.addAll(suspectCards);
        deck.addAll(weaponCards);
        deck.addAll(roomCards);
        Collections.shuffle(deck, rnd);

        int i = 0;
        while (!deck.isEmpty()) {
            Card c = deck.remove(0);
            players.get(i % players.size()).addCard(c);
            i++;
        }System.out.println(solution);
    }

    public Map<Card.CardType, Card> getSolution() { return solution; }

    public Optional<Card> resolveSuggestion(Player suggester, Suggestion suggestion) {
        int startIndex = players.indexOf(suggester);
        int n = players.size();
        for (int offset = 1; offset < n; offset++) {
            Player p = players.get((startIndex + offset) % n);
            Optional<Card> refute = p.refute(suggestion.getSuspect(), suggestion.getWeapon(), suggestion.getRoom());
            if (refute.isPresent()) return refute;
        }
        return Optional.empty();
    }
    public void showSolution() {System.out.println("Solution (hidden): " + solution);}
    public void start() {
        System.out.println("Game setup complete!");
        for (Player p : players) p.showHand();
    }
    public Card getRandomSuspect(){
        Random card=new Random();
        int index=card.nextInt(suspectCards.size());
        return suspectCards.get(index);
    }
    public Card getRandomWeapon(){
        Random card=new Random();
        int index=card.nextInt(weaponCards.size());
        return weaponCards.get(index);
    }
    public Card getRandomRoom(){
        Random card=new Random();
        int index=card.nextInt(roomCards.size());
        return roomCards.get(index);
    }

    public Player getCurrentPlayer() {
        if(players.isEmpty())return null;
        normalizeCurrentTurnIndex();
        return players.get(currentTurnIndex);
    }
    private void normalizeCurrentTurnIndex(){
        if(players.isEmpty())return;
        currentTurnIndex%=players.size();
        if(!players.get(currentTurnIndex).isEliminated())return;
        int n=players.size();
        for(int i=0;i<n;i++){
            if(!players.get(i).isEliminated()){
                currentTurnIndex=i;
                return;
            }
        }
    }
    public int activePlayersCount(){
        int cnt=0;
        for(Player p:players)if(!p.isEliminated())cnt++;
        return cnt;
    }
    public void advanceTurn() {
        int n=players.size();
        for(int i=0;i<=n;i++){
            currentTurnIndex=(currentTurnIndex+1)%n;
            if(players.get(currentTurnIndex).isEliminated())return;
        }
    }
    public List<Player> getActivePlayers() {
        List<Player> active = new ArrayList<>();
        for (Player player : players) { // assuming 'players' is your list of all players
            if (!player.isEliminated()) {
                active.add(player);
            }
        }
        return active;
    }
    public boolean checkAccusation(Suggestion accusation) {
        return solution.get(Card.CardType.SUSPECT).equals(accusation.getSuspect())
                && solution.get(Card.CardType.WEAPON).equals(accusation.getWeapon())
                && solution.get(Card.CardType.ROOM).equals(accusation.getRoom());
    }
}
