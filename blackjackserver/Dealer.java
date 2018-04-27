package blackjackserver;

import java.util.ArrayList;

public class Dealer extends Person{
    public Dealer(){
        super();
        this.name = "DEALER";
    }

    public String getName(){
        return this.name;
    }

    public String cardsToString(boolean hideSecond){
        if(cards.size()>0){
            String s = "";
            if(hideSecond){
                s += cards.get(0).cardToString() + " " + "x:x" + " ";
            } else {
                for(Card c : cards){
                    s += c.cardToString() + " ";
                }
            }
            return s.substring(0, s.length()-1);
        }
        return "";
    }
}