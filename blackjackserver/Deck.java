package blackjackserver;

import java.util.ArrayList;
import java.util.Collections;

public class Deck{
    private ArrayList<Card> cards;
    /**
     * hearts - piros
     * spades - zöld
     * diamonds - tök
     * clubs - makk
     */
    private final char[] color= {'h', 's', 'd', 'c'};

    public Deck(){
        cards = new ArrayList<Card>();
        for(int i=1; i<14; i++){
            for(int j=0; j<4; j++){
                cards.add(new Card(i, color[j]));
            }
        }
        Collections.shuffle(cards);
    }

    public Card takeCard(){
        Card card = cards.get(0);
        cards.remove(0);
        return card;
    }

}