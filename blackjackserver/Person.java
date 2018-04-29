package blackjackserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class Person{
    protected String name;
    private int money;
    protected ArrayList<Card> cards;
    private int realSum;
    
    public Person(){
        this.money = 0;
        this.realSum = 0;
        this.cards = new ArrayList<Card>();
    }

    public int getMoney(){
        return this.money;
    }

    public void setMoney(int money){
        this.money = money;
    }

    public int getRealSum(){
        return this.realSum;
    }

    public void setRealSum(int i){
        this.realSum = i;
    }

    public void addCard(Card c){
        cards.add(c);
    }

    public String getSum(){
        ArrayList<Integer> sums = new ArrayList<Integer>();
        sums.add(0);
        for(Card c : cards){
            int number = c.getNumber();
            if(number != 1){
                int plus;
                if(number > 10){
                    plus = 10;
                } else {
                    plus = number;
                }
                for(int i=0; i<sums.size(); i++){
                    sums.set(i, sums.get(i)+plus);
                }
            } else {
                ArrayList<Integer> copy = new ArrayList<Integer>(sums);
                for(int i=0; i<sums.size(); i++){
                    sums.set(i, sums.get(i)+11);
                }
                for(int i=0; i<copy.size(); i++){
                    copy.set(i, copy.get(i)+1);
                }
                sums.addAll(copy);
            }
        }
        Set<Integer> sumSet = new HashSet<Integer>(sums);
        sums = new ArrayList<Integer>(sumSet);
        Collections.sort(sums, Collections.reverseOrder());
        String s = "";
        for(Integer sum : sums){
            if(sum<=21){
                s += Integer.toString(sum) + "/";
            }
        }
        if(s.length() == 0){
            int def = sums.get(0);
            for(Integer sum : sums){
                if(def>sum) def = sum;

            }
            s += Integer.toString(def) + "/";
        }

        return s.substring(0, s.length()-1);
    }

    public ArrayList<Card> getCards(){
        return this.cards;
    }

    public String cardsToString(){
        if(cards.size()>0){
            String s = "";
            for(Card c : cards){
                s += c.cardToString() + " ";
            }
            return s.substring(0, s.length()-1);
        }
        return "";
    }

    public void clearCards(){
        this.cards.clear();
    }
}