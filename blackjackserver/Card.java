package blackjackserver;

public class Card{
    private int number;
    private char color;

    public Card(int number, char color){
        this.number = number;
        this.color = color;
    }

    public int getNumber(){
        return number;
    }

    public char getColor(){
        return color;
    }

    public String cardToString(){
        return (color + ":" + Integer.toString(number));
    }
}