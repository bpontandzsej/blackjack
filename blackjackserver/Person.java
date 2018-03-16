package blackjackserver;

import java.util.*;

public class Person{
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
        String s = "";
        int[] sum = new int[8];
        sum[0] = 0;
        int n = 1;
        for(Card c : cards){
            int number = c.getNumber();
            if(number != 1){
                for(int i = 0; i<n; i++){
                    if(number > 10){
                        sum[i] += 10;
                    } else {
                        sum[i] += number;
                    }

                }
            } else {
                for(int i = 0; i<n; i++){
                    sum[i+n] = sum[i];
                }
                for(int i = 0; i<n*2; i++){
                    if(i<n){
                        sum[i] += 11;
                    } else {
                        sum[i] += 1;
                    }
                }
                n = n*2;
            }
        }
        for(int i = 0; i<sum.length; i++){
            if(sum[i] <= 21){
                s += Integer.toString(sum[i]) + "/";
            }
        }
        if(s.length() == 0){
            s+= Integer.toString(sum[sum.length-1]);
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