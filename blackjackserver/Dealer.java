package blackjackserver;

public class Dealer extends Person{
    public Dealer(){
        /**
         * SET default VELUES
         */
        this.name = "DEALER";
    }

    /**
     * RETURNS with the dealer's NAME
     */
    public String getName(){
        return this.name;
    }

    /*public void setName(String name){
        this.name = name;
    }*/

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